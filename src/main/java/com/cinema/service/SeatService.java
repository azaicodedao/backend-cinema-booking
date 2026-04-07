package com.cinema.service;

import com.cinema.dto.SeatSelectionDto;
import com.cinema.dto.SeatStatusDto;
import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import com.cinema.entity.Showtime;
import com.cinema.enums.SeatType;
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

            BigDecimal price = calculatePrice(showtime.getPrice(), seat.getSeatType());

            return SeatStatusDto.builder()
                    .seatId(seat.getId())
                    .rowLabel(seat.getRowLabel())
                    .colNumber(seat.getColNumber())
                    .seatType(seat.getSeatType().name())
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

    private BigDecimal calculatePrice(BigDecimal basePrice, SeatType type) {
        if (basePrice == null) return BigDecimal.ZERO;
        if (type == SeatType.VIP) {
            return basePrice.add(new BigDecimal("20000"));
        }
        return basePrice;
    }
}
