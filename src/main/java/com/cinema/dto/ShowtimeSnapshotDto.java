package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO tóm tắt thông tin một suất chiếu dùng để hiển thị trên trang Lịch chiếu (UC06).
 * Chứa đủ thông tin để render nút giờ chiếu và link sang trang chọn ghế.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSnapshotDto {

    /** ID của suất chiếu, dùng để navigate sang trang đặt vé */
    private Integer id;

    /** Thời gian bắt đầu đầy đủ */
    private LocalDateTime startTime;

    /** Chuỗi giờ chiếu định dạng HH:mm, VD: "18:30" – dùng trực tiếp cho nút giờ chiếu */
    private String timeString;

    /**
     * Chuỗi mô tả định dạng chiếu và phòng, VD: "IMAX · Phòng 1" hoặc "2D Phụ đề · Phòng 3".
     * Được tổng hợp từ roomType + roomName.
     */
    private String formatAndRoom;

    /**
     * Trạng thái suất chiếu: "AVAILABLE", "FULL", "UPCOMING", v.v.
     * Frontend dùng để hiển thị badge "Hết chỗ" hoặc disable nút.
     */
    private String status;

    /** Số ghế còn trống (tùy chọn, phục vụ hiển thị "112 ghế trống") */
    private Integer availableSeats;
}
