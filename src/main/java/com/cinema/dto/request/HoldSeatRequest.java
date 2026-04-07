package com.cinema.dto.request;

import lombok.Data;

@Data
public class HoldSeatRequest {
    private Integer seatId;
    private Integer showtimeId;
}
