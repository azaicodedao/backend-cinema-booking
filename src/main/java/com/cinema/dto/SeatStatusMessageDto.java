package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatStatusMessageDto {
    private Integer seatId;
    private Integer showtimeId;
    private String status; // CÒN TRỐNG, ĐANG GIỮ, ĐÃ ĐẶT
    private Integer holdByUserId;
    private LocalDateTime expiredAt;
    private String rowLetter;
    private Integer seatNumber;
}
