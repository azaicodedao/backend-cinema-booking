package com.cinema.controller;

import com.cinema.dto.ChartDataDto;
import com.cinema.dto.DailyRevenueDto;
import com.cinema.dto.SeatStatusMessageDto;
import com.cinema.dto.ShowtimeStatDto;
import com.cinema.dto.response.ApiResponse;
import com.cinema.dto.response.MovieBookingStatsResponseDto;
import com.cinema.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/movies/bookings")
    public ResponseEntity<ApiResponse<MovieBookingStatsResponseDto>> getMovieBookingStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer movieId) {

        MovieBookingStatsResponseDto result = statisticService.getMovieBookingStats(startDate, endDate, movieId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result));
    }

    @GetMapping("/showtimes")
    public ResponseEntity<ApiResponse<Page<ShowtimeStatDto>>> getShowtimeStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer movieId,
            @RequestParam(required = false) Integer roomId) {

        Page<ShowtimeStatDto> result = statisticService.getShowtimeStats(page, size, startDate, endDate, movieId, roomId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result));
    }

    @GetMapping("/showtimes/{id}/seats")
    public ResponseEntity<ApiResponse<List<SeatStatusMessageDto>>> getShowtimeSeatStats(@PathVariable Integer id) {
        List<SeatStatusMessageDto> result = statisticService.getShowtimeSeatStats(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result));
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<List<DailyRevenueDto>>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyRevenueDto> result = statisticService.getDailyRevenue(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result));
    }

    @GetMapping("/revenue/chart")
    public ResponseEntity<ApiResponse<List<ChartDataDto>>> getRevenueChart(
            @RequestParam(defaultValue = "7_days") String period) {

        List<ChartDataDto> result = statisticService.getRevenueChart(period);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result));
    }
}
