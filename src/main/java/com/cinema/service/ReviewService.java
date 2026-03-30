package com.cinema.service;

import com.cinema.dto.ReviewDto;
import com.cinema.entity.Movie;
import com.cinema.entity.Review;
import com.cinema.entity.User;
import com.cinema.mapper.ReviewMapper;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ReviewRepository;
import com.cinema.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    ReviewRepository reviewRepository;
    MovieRepository movieRepository;
    UserRepository userRepository;
    ReviewMapper reviewMapper;

    public List<ReviewDto> getReviewsByMovie(Integer movieId) {
        return reviewRepository.findByMovieId(movieId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public ReviewDto addReview(ReviewDto reviewDto, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Movie movie = movieRepository.findById(reviewDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        boolean alreadyReviewed = reviewRepository.findByMovieId(movie.getId()).stream()
                .anyMatch(r -> r.getUser().getId().equals(user.getId()));

        if (alreadyReviewed) {
            throw new IllegalArgumentException("You have already reviewed this movie.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return reviewMapper.toDto(saved);
    }
}
