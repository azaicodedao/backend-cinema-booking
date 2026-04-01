package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin về một ghế ngồi cụ thể trong phòng chiếu.
 */
public class SeatDto {
    private Integer id;
    private Integer roomId;
    private String rowLetter;
    private Integer seatNumber;
    private String status;
}
