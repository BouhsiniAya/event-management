package com.events.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPurchaseRequest {

    @NotNull(message = "L'ID de l'événement est obligatoire")
    private Long eventId;

    private String paymentIntentId;
}