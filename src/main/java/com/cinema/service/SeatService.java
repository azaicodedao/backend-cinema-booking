package com.cinema.service;

import com.cinema.dto.SeatSelectionDto;
import com.cinema.dto.SeatStatusDto;
import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import com.cinema.entity.Showtime;
import com.cinema.entity.SeatType;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.ShowtimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatService {

    SeatRepository seatRepository;
    ShowtimeRepository showtimeRepository;
    SeatHoldingService seatHoldingService;

    @Transactional(readOnly = true)
    public SeatSelectionDto getSeatSelection(Integer showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        Room room = showtime.getRoom();
        List<Seat> seats = seatRepository.findByRoomId(room.getId());

        // Use SeatHoldingService's logic to get current statuses
        var seatStatuses = seatHoldingService.getSeatsStatusForShowtime(showtimeId);

        Map<String, List<SeatStatusDto>> seatsByRow = seats.stream().map(seat -> {
            // Find status from seatStatuses
            var statusMsg = seatStatuses.stream()
                    .filter(s -> s.getSeatId().equals(seat.getId()))
                    .findFirst()
                    .orElse(null);

            BigDecimal roomSurcharge = (showtime.getRoom() != null && showtime.getRoom().getRoomType() != null)
                    ? showtime.getRoom().getRoomType().getSurcharge() : BigDecimal.ZERO;
            if (roomSurcharge == null) roomSurcharge = BigDecimal.ZERO;

            BigDecimal seatSurcharge = (seat.getSeatType() != null) ? seat.getSeatType().getSurcharge() : BigDecimal.ZERO;
            if (seatSurcharge == null) seatSurcharge = BigDecimal.ZERO;

            BigDecimal price = (showtime.getBasePrice() != null ? showtime.getBasePrice() : BigDecimal.ZERO)
                    .add(roomSurcharge).add(seatSurcharge);

            return SeatStatusDto.builder()
                    .seatId(seat.getId())
                    .rowLabel(seat.getRowLabel())
                    .colNumber(seat.getColNumber())
                    .seatType(seat.getSeatType() != null ? seat.getSeatType().getName() : "NORMAL")
                    .price(price)
                    .status(statusMsg != null ? statusMsg.getStatus() : "AVAILABLE")
                    .holdByUserId(statusMsg != null ? statusMsg.getHoldByUserId() : null)
                    .expiredAt(statusMsg != null ? statusMsg.getExpiredAt() : null)
                    .build();
        }).collect(Collectors.groupingBy(SeatStatusDto::getRowLabel));

        return SeatSelectionDto.builder()
                .showtimeId(showtimeId)
                .movieTitle(showtime.getMovie().getTitle())
                .roomName(room.getName())
                .startTime(showtime.getStartTime())
                .seatsByRow(seatsByRow)
                .build();
    }
}
