package com.events.management.controller;

import com.events.management.dto.request.EventRequest;
import com.events.management.dto.response.EventResponse;
import com.events.management.entity.EventCategory;
import com.events.management.service.impl.EventServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {

    private final EventServiceImpl eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllActiveEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EventCategory category) {
        return ResponseEntity.ok(eventService.searchEvents(keyword, category));
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasAuthority('ROLE_ORGANISATEUR')")
    public ResponseEntity<List<EventResponse>> getMyEvents(
            Authentication authentication) {
        return ResponseEntity.ok(
            eventService.getOrganizerEvents(authentication.getName())
        );
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ORGANISATEUR')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(
                    request, authentication.getName()
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANISATEUR')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
            eventService.updateEvent(id, request, authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANISATEUR')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {
        eventService.deleteEvent(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}