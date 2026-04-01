package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin về sơ đồ ghế ngồi của một phòng chiếu, được dùng để hiển thị khi người dùng chọn chỗ.
 */
public class SeatMapDTO {
    private RoomInfo room;
    private List<SeatInfo> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomInfo {
        private Integer roomId;
        private String name;
        private Integer totalRows;
        private Integer totalCols;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private Integer seatId;
        private String rowLabel;
        private Integer colNumber;
        private String seatType;
        private Double price;
        private String status; // AVAILABLE, HELD, SOLD
    }
}
