package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
    private Integer id;
    private Integer roomId;
    private String rowLetter;
    private Integer seatNumber;
    private String status;
}
