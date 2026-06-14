package com.cinema.service;

import com.cinema.dto.MovieDetailDTO;
import com.cinema.dto.MovieItemDTO;
import com.cinema.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieRatingService {

    ReviewRepository reviewRepository;

    // Thêm thông tin đánh giá vào thẻ phim trang chủ
    public void enrich(MovieItemDTO dto, Integer movieId) {
        RatingSummary summary = getSummary(movieId);
        dto.setAverageRating(summary.averageRating());
        dto.setReviewCount(summary.reviewCount());
        dto.setRatingDistribution(summary.ratingDistribution());
    }

    // Thêm thông tin đánh giá vào trang chi tiết phim
    public void enrich(MovieDetailDTO dto, Integer movieId) {
        RatingSummary summary = getSummary(movieId);
        dto.setAverageRating(summary.averageRating());
        dto.setReviewCount(summary.reviewCount());
        dto.setRatingDistribution(summary.ratingDistribution());
    }

    // Lấy thông tin đánh giá phục vụ cho trang đánh giá phim
    private RatingSummary getSummary(Integer movieId) {
        Double avg = reviewRepository.getAverageRatingByMovieId(movieId);
        Long count = reviewRepository.getReviewCountByMovieId(movieId);
        List<Object[]> distribution = reviewRepository.getRatingDistributionByMovieId(movieId);

        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (Object[] row : distribution) {
            ratingDistribution.put((Integer) row[0], ((Long) row[1]).intValue());
        }

        return new RatingSummary(
                avg != null ? avg : 0.0,
                count != null ? count.intValue() : 0,
                ratingDistribution);
    }

    private record RatingSummary(Double averageRating, Integer reviewCount, Map<Integer, Integer> ratingDistribution) {
    }
}
