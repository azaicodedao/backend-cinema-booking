package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.ReviewDto;
import com.cinema.security.services.UserDetailsImpl;
import com.cinema.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<RestResponse<List<ReviewDto>>> getReviewsByMovie(@PathVariable Integer movieId) {
        List<ReviewDto> reviews = reviewService.getReviewsByMovie(movieId);
        return ResponseEntity.ok(RestResponse.success(reviews, "Fetched reviews successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<ReviewDto>> addReview(@RequestBody ReviewDto reviewDto) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        try {
            ReviewDto savedReview = reviewService.addReview(reviewDto, userDetails.getId());
            return ResponseEntity.ok(RestResponse.success(savedReview, "Review added successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "An error occurred while adding the review"));
        }
    }
}
