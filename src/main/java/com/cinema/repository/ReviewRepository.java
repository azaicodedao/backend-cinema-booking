package com.cinema.repository;

import com.cinema.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByMovieId(Integer movieId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId")
    Double getAverageRatingByMovieId(Integer movieId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.id = :movieId")
    Long getReviewCountByMovieId(Integer movieId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.movie.id = :movieId GROUP BY r.rating")
    List<Object[]> getRatingDistributionByMovieId(Integer movieId);

    boolean existsByBookingId(Integer bookingId);
}
