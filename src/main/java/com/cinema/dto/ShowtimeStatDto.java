package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeStatDto {
    private Integer showtimeId;
    private String movieTitle;
    private String roomName;
    private LocalDateTime showTime;
    private Long totalSeats;
    private Long soldSeats;
    private BigDecimal revenue;
}
