package com.events.management.dto.request;

import com.events.management.entity.EventCategory;
import jakarta.validation.constraints.*;
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
public class EventRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 200)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @NotBlank(message = "Le lieu est obligatoire")
    private String location;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1)
    private Integer maxCapacity;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0")
    private BigDecimal ticketPrice;

    private String imageUrl;

    @NotNull(message = "La catégorie est obligatoire")
    private EventCategory category;
}