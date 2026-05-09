package com.cinema.controller;

import com.cinema.dto.MovieBookingStatDto;
import com.cinema.dto.response.ApiResponse;
import com.cinema.enums.BookingStatus;
import com.cinema.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final BookingRepository bookingRepository;

    @GetMapping("/movies/bookings")
    public ResponseEntity<ApiResponse<Page<MovieBookingStatDto>>> getMovieBookingStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer movieId) {

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now().plusYears(10);

        Pageable pageable = PageRequest.of(page, size);
        Page<MovieBookingStatDto> stats = bookingRepository.getMovieBookingStats(BookingStatus.CONFIRMED, start, end, movieId, pageable);

        return ResponseEntity.ok(new ApiResponse<>(true, "Success", stats));
    }
}
