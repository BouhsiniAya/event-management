package com.events.management.dto.response;

import com.events.management.entity.TicketStatus;
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
public class TicketResponse {

    private Long id;
    private String ticketCode;
    private String qrCodePath;
    private BigDecimal purchasePrice;
    private TicketStatus status;
    private LocalDateTime purchasedAt;
    private boolean used;
    private Long eventId;
    private String eventTitle;
    private String eventLocation;
    private LocalDateTime eventStartDate;
    private String userName;
    private String userEmail;
}