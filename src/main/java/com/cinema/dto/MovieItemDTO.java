package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin tóm tắt về phim được sử dụng để hiển thị trong danh sách
 * hoặc trang chủ.
 */
public class MovieItemDTO {
    private Integer id;
    private String title;
    private String posterUrl;
    private Integer duration;
    private Integer ageRating;
    private String trailerUrl;
    private String status;
    private LocalDate releaseDate;

    private Double averageRating;
    private Integer reviewCount;
    private Map<Integer, Integer> ratingDistribution;

    private Boolean isFeatured;
    private List<GenreDto> genres;
}
