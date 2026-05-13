package com.events.management.dto.response;

import com.events.management.entity.EventCategory;
import com.events.management.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private Integer maxCapacity;
    private Integer availableSeats;
    private BigDecimal ticketPrice;
    private String imageUrl;
    private EventStatus status;
    private EventCategory category;
    private String organizerName;
    private Long organizerId;
    private LocalDateTime createdAt;
}