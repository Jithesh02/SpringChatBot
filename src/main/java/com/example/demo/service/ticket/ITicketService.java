package com.example.demo.service.ticket;

import com.example.demo.dto.TicketDTO;
import com.example.demo.model.Converstion;
import com.example.demo.model.Ticket;

import java.util.List;

public interface ITicketService {
    Ticket createTicketForConversation(Converstion converstion);
    TicketDTO getTicketById(Long ticketId);
    TicketDTO resolveTicket(Long ticketId, String resolutionDetails);
    List<TicketDTO> getAllTickets();
}
