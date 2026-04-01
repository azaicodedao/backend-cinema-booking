package com.cinema.dto.request;

import lombok.Data;

@Data
/**
 * DTO chứa yêu cầu gửi đánh giá phim từ người dùng, bao gồm mã đặt vé, mã phim, số sao và bình luận.
 */
public class SubmitReviewRequest {
    private Integer bookingId;
    private Integer movieId;
    private Integer rating; // 1 - 5
    private String comment;
}
