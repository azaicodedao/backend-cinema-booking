package com.cinema.repository;

import com.cinema.entity.Movie;
import com.cinema.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByStatusAndIsFeatured(MovieStatus status, Boolean isFeatured);
    List<Movie> findByStatus(MovieStatus status);
    List<Movie> findByIsFeatured(Boolean isFeatured);
    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByGenresId(Integer genreId);
    List<Movie> findByGenresIdAndTitleContainingIgnoreCase(Integer genreId, String title);
    List<Movie> findByStatusAndGenresId(MovieStatus status, Integer genreId);
    List<Movie> findByStatusAndGenresIdAndTitleContainingIgnoreCase(MovieStatus status, Integer genreId, String title);
}
