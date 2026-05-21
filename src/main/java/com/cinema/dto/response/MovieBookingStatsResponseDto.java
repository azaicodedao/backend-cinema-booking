package com.cinema.dto.response;

import com.cinema.dto.MovieBookingStatDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO này dùng để lưu trữ thông tin thống kê doanh thu theo phim.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieBookingStatsResponseDto {
    private List<MovieBookingStatDto> stats;
    private Long totalBookings;
    private BigDecimal totalRevenue;
}
