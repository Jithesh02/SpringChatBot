package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String referenceNumber;
    private String resolutionDetails;
    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @OneToOne
    @JoinColumn(name = "conversation_Id")
    private Converstion converstion;



}
