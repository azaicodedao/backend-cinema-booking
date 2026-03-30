package com.cinema.repository;

import com.cinema.entity.Booking;
import com.cinema.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Optional<Ticket> findByQrCode(String qrCode); // changed from ticketCode if mismatch

    List<Ticket> findByBooking(Booking booking);
}
