package com.events.management.controller;

import com.events.management.dto.request.PaymentRequest;
import com.events.management.dto.request.TicketPurchaseRequest;
import com.events.management.dto.response.TicketResponse;
import com.events.management.service.impl.TicketServiceImpl;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TicketController {

    private final TicketServiceImpl ticketService;

    @PostMapping("/payment/create-intent")
    @PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @Valid @RequestBody PaymentRequest request) throws StripeException {
        return ResponseEntity.ok(ticketService.createPaymentIntent(request));
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
    public ResponseEntity<TicketResponse> purchaseTicket(
            @Valid @RequestBody TicketPurchaseRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
            ticketService.purchaseTicket(request, authentication.getName())
        );
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            Authentication authentication) {
        return ResponseEntity.ok(
            ticketService.getUserTickets(authentication.getName())
        );
    }

    @PostMapping("/scan/{ticketCode}")
    @PreAuthorize("hasAuthority('ROLE_ORGANISATEUR')")
    public ResponseEntity<TicketResponse> scanTicket(
            @PathVariable String ticketCode,
            Authentication authentication) {
        return ResponseEntity.ok(
            ticketService.scanTicket(ticketCode, authentication.getName())
        );
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANISATEUR')")
    public ResponseEntity<List<TicketResponse>> getEventTickets(
            @PathVariable Long eventId,
            Authentication authentication) {
        return ResponseEntity.ok(
            ticketService.getEventTickets(eventId, authentication.getName())
        );
    }
}