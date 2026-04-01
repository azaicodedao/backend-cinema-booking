package com.cinema.dto.request;

import lombok.Data;

import java.util.List;

@Data
/**
 * DTO chứa yêu cầu đặt vé từ phía người dùng, bao gồm suất chiếu, danh sách ghế và tổng tiền.
 */
public class BookingRequestDto {
    private Integer showtimeId;
    private List<Integer> seatIds;
    private Double totalPrice;
}
