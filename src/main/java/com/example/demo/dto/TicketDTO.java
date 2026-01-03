package com.example.demo.dto;

import com.example.demo.model.Converstion;
import com.example.demo.model.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TicketDTO {
    private Long id;
    private TicketStatus ticketStatus;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long productOrderNumber;
    private String referenceNumber;
    private String resolutionDetails;
    private Converstion conversation;

}
