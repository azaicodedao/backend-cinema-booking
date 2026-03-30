package com.cinema.service;

import com.cinema.dto.request.BookingRequestDto;
import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import com.cinema.enums.TicketStatus;
import com.cinema.enums.BookingStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    BookingRepository bookingRepository;
    ShowtimeRepository showtimeRepository;
    SeatRepository seatRepository;
    TicketRepository ticketRepository;
    UserRepository userRepository;
    SeatHoldingService SeatHoldingService;

    @Transactional
    public Booking createBooking(BookingRequestDto request, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.stream().anyMatch(s -> !s.getRoom().getId().equals(showtime.getRoom().getId()))) {
            throw new IllegalArgumentException("Seats do not belong to the correct room.");
        }

        if (!SeatHoldingService.areSeatsHeldByUser(request.getSeatIds(), request.getShowtimeId(), userId)) {
            throw new IllegalArgumentException("You must hold all selected seats before booking. Please select seats first.");
        }

        BigDecimal totalPrice = showtime.getPrice().multiply(BigDecimal.valueOf(seats.size()));

        Booking booking = new Booking();
        booking.setBookingCode("BKG" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setBooking(savedBooking);
            ticket.setSeat(seat);
            ticket.setQrCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            ticket.setStatus(TicketStatus.VALID); // changed from BOOKED if using TicketStatus
            ticketRepository.save(ticket);
        }

        SeatHoldingService.convertHoldsToBooked(request.getSeatIds(), request.getShowtimeId());

        return savedBooking;
    }

    @Transactional
    public void payBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
                
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Booking already paid");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        List<Ticket> tickets = ticketRepository.findByBooking(booking);
        for (Ticket t : tickets) {
            t.setStatus(TicketStatus.VALID);
            ticketRepository.save(t);
        }
    }
}
