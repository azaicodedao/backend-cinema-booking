package com.cinema.dto.request;

import lombok.Data;

@Data
public class SeatHoldingRequestDto {
    private Integer showtimeId;
    private Integer seatId;
}
