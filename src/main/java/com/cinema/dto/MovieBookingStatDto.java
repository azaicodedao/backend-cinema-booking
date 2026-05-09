package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieBookingStatDto {
    private Integer movieId;
    private String title;
    private Long totalBookings;
    private BigDecimal totalRevenue;
}
