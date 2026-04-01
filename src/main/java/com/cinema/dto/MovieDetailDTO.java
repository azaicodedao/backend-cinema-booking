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
 * DTO chứa thông tin chi tiết của một bộ phim, bao gồm đánh giá, rạp chiếu và thể loại.
 */
public class MovieDetailDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer duration;
    private String country;
    private Integer ageRating;
    private String director;
    private String actors;
    private String trailerUrl;
    private String posterUrl;
    private LocalDate releaseDate;
    private String status;
    
    private Double averageRating;
    private Integer reviewCount;
    private Map<Integer, Integer> ratingDistribution;
    
    private List<GenreDto> genres;
}
