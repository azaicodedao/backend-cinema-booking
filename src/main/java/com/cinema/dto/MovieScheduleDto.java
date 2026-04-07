package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO tổng hợp dữ liệu một bộ phim kèm danh sách suất chiếu của ngày được chọn,
 * phục vụ màn hình Lịch chiếu (UC06).
 * <p>
 * Cấu trúc này cho phép Frontend render trực tiếp card phim (.mst-card) mà không cần
 * tự nhóm (group) dữ liệu phía client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieScheduleDto {

    /** ID bộ phim */
    private Integer movieId;

    /** Tên bộ phim */
    private String title;

    /** URL ảnh poster */
    private String posterUrl;

    /** Danh sách tên thể loại, phân cách bằng dấu phẩy. VD: "Hành động, Phiêu lưu" */
    private String genres;

    /** Mức giới hạn tuổi. VD: 16 → "T16+" */
    private Integer ageRating;

    /**
     * Điểm đánh giá trung bình của phim (từ bảng reviews).
     * Nếu chưa có đánh giá nào, mặc định là null.
     */
    private Double rating;

    /** Thời lượng phim tính bằng phút */
    private Integer duration;

    /** Danh sách các suất chiếu trong ngày được chọn, đã sắp xếp theo giờ tăng dần */
    private List<ShowtimeSnapshotDto> showtimes;
}
