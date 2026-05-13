package com.events.management.service.impl;

import com.events.management.dto.request.PaymentRequest;
import com.events.management.dto.request.TicketPurchaseRequest;
import com.events.management.dto.response.TicketResponse;
import com.events.management.entity.*;
import com.events.management.exception.ResourceNotFoundException;
import com.events.management.repository.EventRepository;
import com.events.management.repository.TicketRepository;
import com.events.management.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketServiceImpl {

    private static final Logger log =
            LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final QrCodeServiceImpl qrCodeService;
    private final EmailServiceImpl emailService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public TicketServiceImpl(TicketRepository ticketRepository,
                              EventRepository eventRepository,
                              UserRepository userRepository,
                              QrCodeServiceImpl qrCodeService,
                              EmailServiceImpl emailService) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
    }

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Map<String, String> createPaymentIntent(PaymentRequest request)
            throws StripeException {

        long amountInCents = request.getAmount()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.getCurrency())
                .setDescription("Billet événement #" + request.getEventId())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return Map.of(
                "clientSecret", paymentIntent.getClientSecret(),
                "paymentIntentId", paymentIntent.getId()
        );
    }

    public TicketResponse purchaseTicket(TicketPurchaseRequest request,
                                          String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Utilisateur non trouvé")
                );

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Événement", "id", request.getEventId()
                    )
                );

        if (event.getAvailableSeats() <= 0) {
            throw new RuntimeException("Plus de places disponibles");
        }
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new RuntimeException("Événement non disponible");
        }
        if (ticketRepository.hasValidTicket(user, event)) {
            throw new RuntimeException(
                "Vous avez déjà un billet pour cet événement"
            );
        }

        String ticketCode = UUID.randomUUID().toString()
                .toUpperCase().replace("-", "").substring(0, 16);

        String qrCodePath = null;
        byte[] qrCodeBytes = null;
        try {
            qrCodePath = qrCodeService.generateQrCode(ticketCode);
            qrCodeBytes = qrCodeService.generateQrCodeBytes(ticketCode);
        } catch (Exception e) {
            log.error("Erreur QR Code : {}", e.getMessage());
        }

        Ticket ticket = Ticket.builder()
                .ticketCode(ticketCode)
                .qrCodePath(qrCodePath)
                .purchasePrice(event.getTicketPrice())
                .status(TicketStatus.VALID)
                .stripePaymentIntentId(request.getPaymentIntentId())
                .used(false)
                .event(event)
                .user(user)
                .build();

        ticketRepository.save(ticket);

        event.setAvailableSeats(event.getAvailableSeats() - 1);
        if (event.getAvailableSeats() == 0) {
            event.setStatus(EventStatus.SOLD_OUT);
        }
        eventRepository.save(event);

        if (qrCodeBytes != null) {
            emailService.sendTicketConfirmation(
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    event.getTitle(),
                    ticketCode,
                    qrCodeBytes
            );
        }

        return mapToResponse(ticket);
    }

    public TicketResponse scanTicket(String ticketCode,
                                      String organizerEmail) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Billet", "code", ticketCode)
                );

        if (!ticket.getEvent().getOrganizer().getEmail()
                .equals(organizerEmail)) {
            throw new RuntimeException("Non autorisé à scanner ce billet");
        }
        if (ticket.isUsed()) {
            throw new RuntimeException(
                "Billet déjà utilisé le " + ticket.getUsedAt()
            );
        }
        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new RuntimeException(
                "Billet non valide : " + ticket.getStatus()
            );
        }

        ticket.setUsed(true);
        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return mapToResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getUserTickets(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Utilisateur non trouvé")
                );
        return ticketRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getEventTickets(Long eventId,
                                                  String organizerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Événement", "id", eventId)
                );
        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("Accès non autorisé");
        }
        return ticketRepository.findByEvent(event)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .qrCodePath(ticket.getQrCodePath())
                .purchasePrice(ticket.getPurchasePrice())
                .status(ticket.getStatus())
                .purchasedAt(ticket.getPurchasedAt())
                .used(ticket.isUsed())
                .eventId(ticket.getEvent().getId())
                .eventTitle(ticket.getEvent().getTitle())
                .eventLocation(ticket.getEvent().getLocation())
                .eventStartDate(ticket.getEvent().getStartDate())
                .userName(ticket.getUser().getFirstName()
                        + " " + ticket.getUser().getLastName())
                .userEmail(ticket.getUser().getEmail())
                .build();
    }
}