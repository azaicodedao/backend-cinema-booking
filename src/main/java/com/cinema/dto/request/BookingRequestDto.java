package com.cinema.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
/**
 * DTO chứa yêu cầu đặt vé từ phía người dùng, bao gồm suất chiếu và danh sách ghế.
 * Tổng tiền được tính toán hoàn toàn tại Server (không nhận từ Client).
 */
public class BookingRequestDto {

    @NotNull(message = "Showtime ID is required")
    private Integer showtimeId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<Integer> seatIds;
}
