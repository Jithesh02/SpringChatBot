package com.example.demo.service.ticket;

import com.example.demo.dto.TicketDTO;
import com.example.demo.model.Converstion;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService implements ITicketService {

    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Ticket createTicketForConversation(Converstion converstion) {
        Ticket ticket = new Ticket();
        ticket.setConverstion(converstion);
        ticket.setTicketStatus(TicketStatus.OPENED);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setResolvedAt(null);
        ticket.setReferenceNumber(generateRandomAlphanumeric());
        return ticketRepository.save(ticket);
    }

    @Override
    public TicketDTO getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> objectMapper.convertValue(ticket, TicketDTO.class))
                .orElseThrow(() -> new EntityNotFoundException("Ticket with id" + ticketId + "not found"));
    }

    @Override
    public TicketDTO resolveTicket(Long ticketId, String resolutionDetails) {
        return null;
    }

    @Override
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(ticket -> objectMapper.convertValue(ticket, TicketDTO.class))
                .collect(Collectors.toList());
    }

    private String generateRandomAlphanumeric() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z').filteredBy(Character::isLetterOrDigit)
                .get();
        return generator.generate(10).toUpperCase();
    }
}
