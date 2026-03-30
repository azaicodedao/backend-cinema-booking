package com.cinema.service;

import com.cinema.dto.SeatStatusMessageDto;
import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import com.cinema.enums.BookingStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatHoldingService {

    SeatHoldingRepository seatHoldingRepository;
    SeatRepository seatRepository;
    ShowtimeRepository showtimeRepository;
    TicketRepository ticketRepository;
    BookingRepository bookingRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    static final int HOLD_DURATION_MINUTES = 10;

    @Transactional
    public SeatStatusMessageDto holdSeat(Integer seatId, Integer showtimeId, Integer userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        if (!seat.getRoom().getId().equals(showtime.getRoom().getId())) {
            throw new IllegalArgumentException("Seat does not belong to the showtime's room");
        }

        if (isSeatBookedForShowtime(seatId, showtimeId)) {
            throw new IllegalArgumentException("Seat is already booked for this showtime");
        }

        Optional<SeatHolding> existingHold = seatHoldingRepository.findBySeatIdAndShowtimeId(seatId, showtimeId);
        if (existingHold.isPresent()) {
            SeatHolding hold = existingHold.get();
            if (hold.getExpiredAt().isAfter(LocalDateTime.now())) {
                if (hold.getUser().getId().equals(userId)) {
                    hold.setExpiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));
                    seatHoldingRepository.save(hold);
                } else {
                    throw new IllegalArgumentException("Seat is already held by another user");
                }
            } else {
                hold.setUser(userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")));
                hold.setExpiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));
                seatHoldingRepository.save(hold);
            }
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            SeatHolding seatHolding = SeatHolding.builder()
                    .seat(seat)
                    .showtime(showtime)
                    .user(user)
                    .expiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES))
                    .build();
            seatHoldingRepository.save(seatHolding);
        }

        SeatStatusMessageDto message = SeatStatusMessageDto.builder()
                .seatId(seatId)
                .showtimeId(showtimeId)
                .status("HOLDING")
                .holdByUserId(userId)
                .expiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES))
                .rowLetter(seat.getRowLabel())
                .seatNumber(seat.getColNumber())
                .build();

        messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId, message);

        return message;
    }

    @Transactional
    public SeatStatusMessageDto releaseSeat(Integer seatId, Integer showtimeId, Integer userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        Optional<SeatHolding> existingHold = seatHoldingRepository.findBySeatIdAndShowtimeId(seatId, showtimeId);
        if (existingHold.isPresent()) {
            SeatHolding hold = existingHold.get();
            if (!hold.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("You can only release your own held seats");
            }
            seatHoldingRepository.delete(hold);
        }

        SeatStatusMessageDto message = SeatStatusMessageDto.builder()
                .seatId(seatId)
                .showtimeId(showtimeId)
                .status("AVAILABLE")
                .holdByUserId(null)
                .expiredAt(null)
                .rowLetter(seat.getRowLabel())
                .seatNumber(seat.getColNumber())
                .build();

        messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId, message);

        return message;
    }

    @Transactional(readOnly = true)
    public List<SeatStatusMessageDto> getSeatsStatusForShowtime(Integer showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        List<Seat> seats = seatRepository.findByRoomId(showtime.getRoom().getId());

        List<SeatHolding> activeHolds = seatHoldingRepository
                .findByShowtimeIdAndExpiredAtAfter(showtimeId, LocalDateTime.now());

        Set<Integer> bookedSeatIds = getBookedSeatIdsForShowtime(showtimeId);

        List<SeatStatusMessageDto> result = new ArrayList<>();
        for (Seat seat : seats) {
            SeatStatusMessageDto.SeatStatusMessageDtoBuilder builder = SeatStatusMessageDto.builder()
                    .seatId(seat.getId())
                    .showtimeId(showtimeId)
                    .rowLetter(seat.getRowLabel())
                    .seatNumber(seat.getColNumber());

            if (bookedSeatIds.contains(seat.getId())) {
                builder.status("BOOKED");
            } else {
                Optional<SeatHolding> hold = activeHolds.stream()
                        .filter(h -> h.getSeat().getId().equals(seat.getId()))
                        .findFirst();
                if (hold.isPresent()) {
                    builder.status("HOLDING")
                            .holdByUserId(hold.get().getUser().getId())
                            .expiredAt(hold.get().getExpiredAt());
                } else {
                    builder.status("AVAILABLE");
                }
            }

            result.add(builder.build());
        }

        return result;
    }

    @Transactional
    public void releaseExpiredHolds() {
        List<SeatHolding> expiredHolds = seatHoldingRepository.findByExpiredAtBefore(LocalDateTime.now());

        if (expiredHolds.isEmpty()) return;

        var holdsByShowtime = expiredHolds.stream()
                .collect(Collectors.groupingBy(h -> h.getShowtime().getId()));

        seatHoldingRepository.deleteAll(expiredHolds);

        for (var entry : holdsByShowtime.entrySet()) {
            Integer showtimeId = entry.getKey();
            for (SeatHolding hold : entry.getValue()) {
                SeatStatusMessageDto message = SeatStatusMessageDto.builder()
                        .seatId(hold.getSeat().getId())
                        .showtimeId(showtimeId)
                        .status("AVAILABLE")
                        .holdByUserId(null)
                        .expiredAt(null)
                        .rowLetter(hold.getSeat().getRowLabel())
                        .seatNumber(hold.getSeat().getColNumber())
                        .build();
                messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId, message);
            }
        }
    }

    public boolean areSeatsHeldByUser(List<Integer> seatIds, Integer showtimeId, Integer userId) {
        List<SeatHolding> userHolds = seatHoldingRepository.findByUserIdAndShowtimeId(userId, showtimeId);
        Set<Integer> heldSeatIds = userHolds.stream()
                .filter(h -> h.getExpiredAt().isAfter(LocalDateTime.now()))
                .map(h -> h.getSeat().getId())
                .collect(Collectors.toSet());
        return heldSeatIds.containsAll(seatIds);
    }

    @Transactional
    public void convertHoldsToBooked(List<Integer> seatIds, Integer showtimeId) {
        for (Integer seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            seatHoldingRepository.findBySeatIdAndShowtimeId(seatId, showtimeId)
                    .ifPresent(seatHoldingRepository::delete);

            if (seat != null) {
                SeatStatusMessageDto message = SeatStatusMessageDto.builder()
                        .seatId(seatId)
                        .showtimeId(showtimeId)
                        .status("BOOKED")
                        .holdByUserId(null)
                        .expiredAt(null)
                        .rowLetter(seat.getRowLabel())
                        .seatNumber(seat.getColNumber())
                        .build();
                messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId, message);
            }
        }
    }

    private boolean isSeatBookedForShowtime(Integer seatId, Integer showtimeId) {
        return getBookedSeatIdsForShowtime(showtimeId).contains(seatId);
    }

    private Set<Integer> getBookedSeatIdsForShowtime(Integer showtimeId) {
        List<Booking> bookings = bookingRepository.findByShowtimeId(showtimeId);
        return bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .flatMap(b -> ticketRepository.findByBooking(b).stream())
                .map(t -> t.getSeat().getId())
                .collect(Collectors.toSet());
    }
}
