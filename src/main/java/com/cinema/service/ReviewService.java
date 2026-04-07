package com.cinema.service;

import com.cinema.dto.ReviewDto;
import com.cinema.dto.ReviewSummaryDto;
import com.cinema.entity.Booking;
import com.cinema.entity.Movie;
import com.cinema.entity.Review;
import com.cinema.entity.User;
import com.cinema.mapper.ReviewMapper;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ReviewRepository;
import com.cinema.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    ReviewRepository reviewRepository;
    MovieRepository movieRepository;
    UserRepository userRepository;
    BookingRepository bookingRepository;
    ReviewMapper reviewMapper;

    public List<ReviewDto> getReviewsByMovie(Integer movieId) {
        return reviewRepository.findByMovieId(movieId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public ReviewDto addReview(ReviewDto reviewDto, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (reviewDto.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required for review.");
        }

        Booking booking = bookingRepository.findById(reviewDto.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Security check: booking must belong to the user
        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only review your own bookings.");
        }

        // Integrity check: booking must be for the specified movie
        if (!booking.getShowtime().getMovie().getId().equals(reviewDto.getMovieId())) {
             throw new IllegalArgumentException("Booking does not match the movie being reviewed.");
        }

        // Validity check: one review per booking
        if (reviewRepository.existsByBookingId(reviewDto.getBookingId())) {
            throw new IllegalArgumentException("You have already reviewed this booking.");
        }

        Movie movie = movieRepository.findById(reviewDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        // Mapping using MapStruct
        Review review = reviewMapper.toEntity(reviewDto);
        review.setUser(user);
        review.setMovie(movie);
        review.setBooking(booking);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return reviewMapper.toDto(saved);
    }

    public ReviewSummaryDto getMovieReviewSummary(Integer movieId) {
        Double avg = reviewRepository.getAverageRatingByMovieId(movieId);
        Long count = reviewRepository.getReviewCountByMovieId(movieId);
        List<Object[]> distRaw = reviewRepository.getRatingDistributionByMovieId(movieId);

        Map<Integer, Long> dist = new HashMap<>();
        // Initialize all stars 1-5 with 0
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);
        
        if (distRaw != null) {
            for (Object[] obj : distRaw) {
                Integer rating = (Integer) obj[0];
                Long c = (Long) obj[1];
                dist.put(rating, c);
            }
        }

        return ReviewSummaryDto.builder()
                .averageRating(avg != null ? avg : 0.0)
                .totalReviews(count != null ? count : 0L)
                .ratingDistribution(dist)
                .build();
    }
}
