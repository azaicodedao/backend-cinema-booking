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
 * DTO chứa thông tin lịch sử đặt vé của người dùng, bao gồm thông tin phim, phòng chiếu và trạng thái giao dịch.
 */
public class BookingHistoryDTO {
    private Integer bookingId;
    private String bookingCode;
    private String movieTitle;
    private String roomName;
    private LocalDateTime showtimeStart;
    private List<String> seatLabels;
    private Integer numberOfTickets;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private Boolean hasReviewed;
}
