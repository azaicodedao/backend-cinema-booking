package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin về phòng chiếu phim, bao gồm số lượng ghế và loại phòng.
 */
public class RoomDto {
    private Integer id;
    private String name;
    private Integer totalRows;
    private Integer totalCols;
    private Integer totalSeats;
    private String type;
    private String status;
}
