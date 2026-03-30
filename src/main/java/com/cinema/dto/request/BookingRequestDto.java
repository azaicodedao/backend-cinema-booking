package com.cinema.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequestDto {
    private Integer showtimeId;
    private List<Integer> seatIds;
}
