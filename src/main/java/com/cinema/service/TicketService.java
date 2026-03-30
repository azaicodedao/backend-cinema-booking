package com.cinema.service;

import com.cinema.entity.Ticket;
import com.cinema.repository.TicketRepository;
import com.cinema.enums.TicketStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketService {

    TicketRepository ticketRepository;

    public String checkInTicket(String qrCode) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new IllegalArgumentException("Ticket is already checked in.");
        }

        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new IllegalArgumentException("Ticket is not valid.");
        }

        ticket.setStatus(TicketStatus.USED);
        ticketRepository.save(ticket);

        return "Ticket checked in successfully!";
    }
}
