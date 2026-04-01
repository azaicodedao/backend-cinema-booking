package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin nhóm các suất chiếu theo phòng, dùng để hiển thị lịch chiếu trong ngày.
 */
public class ShowtimeGroupDTO {
    private Integer roomId;
    private String roomName;
    private String roomType;
    private List<ShowtimeInfo> showtimes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowtimeInfo {
        private Integer id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private Integer availableSeats;
    }
}
