package com.cinema.dto;
import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution; // Star rating -> Count
}
