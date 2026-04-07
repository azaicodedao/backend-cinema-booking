package com.cinema.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectionDto {
    private Integer showtimeId;
    private String movieTitle;
    private String roomName;
    private LocalDateTime startTime;
    private Map<String, List<SeatStatusDto>> seatsByRow;
}
