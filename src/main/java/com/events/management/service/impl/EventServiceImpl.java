package com.events.management.service.impl;

import com.events.management.dto.request.EventRequest;
import com.events.management.dto.response.EventResponse;
import com.events.management.entity.*;
import com.events.management.exception.ResourceNotFoundException;
import com.events.management.repository.EventRepository;
import com.events.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository,
                             UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public EventResponse createEvent(EventRequest request,
                                      String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Utilisateur non trouvé")
                );

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .maxCapacity(request.getMaxCapacity())
                .availableSeats(request.getMaxCapacity())
                .ticketPrice(request.getTicketPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .status(EventStatus.ACTIVE)
                .organizer(organizer)
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    public EventResponse updateEvent(Long eventId,
                                      EventRequest request,
                                      String organizerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Événement", "id", eventId)
                );

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException(
                "Vous n'êtes pas l'organisateur de cet événement"
            );
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setMaxCapacity(request.getMaxCapacity());
        event.setTicketPrice(request.getTicketPrice());
        event.setImageUrl(request.getImageUrl());
        event.setCategory(request.getCategory());

        return mapToResponse(eventRepository.save(event));
    }

    public void deleteEvent(Long eventId, String organizerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Événement", "id", eventId)
                );

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException(
                "Vous n'êtes pas l'organisateur de cet événement"
            );
        }
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllActiveEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Événement", "id", id)
                );
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getOrganizerEvents(String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Utilisateur non trouvé")
                );
        return eventRepository.findByOrganizer(organizer)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String keyword,
                                             EventCategory category) {
        return eventRepository
                .searchEvents(keyword, category, EventStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .maxCapacity(event.getMaxCapacity())
                .availableSeats(event.getAvailableSeats())
                .ticketPrice(event.getTicketPrice())
                .imageUrl(event.getImageUrl())
                .status(event.getStatus())
                .category(event.getCategory())
                .organizerName(event.getOrganizer().getFirstName()
                        + " " + event.getOrganizer().getLastName())
                .organizerId(event.getOrganizer().getId())
                .createdAt(event.getCreatedAt())
                .build();
    }
}