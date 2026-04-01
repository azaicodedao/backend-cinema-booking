package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin về đánh giá và bình luận của người dùng cho một bộ phim.
 */
public class ReviewDto {
    private Integer id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private ReviewUser user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewUser {
        private Integer id;
        private String fullName;
        private String avatarUrl;
    }
}
