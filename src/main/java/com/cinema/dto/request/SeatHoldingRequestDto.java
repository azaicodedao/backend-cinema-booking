package com.cinema.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
/**
 * DTO chứa yêu cầu giữ chỗ tạm thời cho các ghế đã chọn trong một suất chiếu.
 */
public class SeatHoldingRequestDto {

    @NotNull(message = "Showtime ID is required")
    private Integer showtimeId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<Integer> seatIds;
}
