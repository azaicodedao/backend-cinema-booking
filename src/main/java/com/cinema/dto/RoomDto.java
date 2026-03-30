package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Integer id;
    private String name;
    private Integer totalRows;
    private Integer totalCols;
    private Integer totalSeats;
    private String roomType;
}
