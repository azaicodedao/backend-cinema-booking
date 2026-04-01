package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin về một suất chiếu cụ thể, bao gồm thời gian bắt đầu, kết thúc và giá vé.
 */
public class ShowtimeDto {
    private Integer id;
    private Integer movieId;
    private String movieTitle;
    private Integer roomId;
    private String roomName;
    private LocalDate showDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;
}
