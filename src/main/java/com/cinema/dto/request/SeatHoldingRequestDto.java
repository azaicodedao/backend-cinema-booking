package com.cinema.dto.request;

import lombok.Data;
import java.util.List;

@Data
/**
 * DTO chứa yêu cầu giữ chỗ tạm thời cho các ghế đã chọn trong một suất chiếu.
 */
public class SeatHoldingRequestDto {
    private Integer showtimeId;
    private List<Integer> seatIds;
}
