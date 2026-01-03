package com.example.demo.repository;

import com.example.demo.model.Customer;
import com.example.demo.model.Ticket;
import org.apache.logging.log4j.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
}
