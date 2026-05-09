package com.cinema.repository;

import com.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.cinema.enums.BookingStatus;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cinema.dto.MovieBookingStatDto;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
       List<Booking> findByUserId(Integer userId);

       List<Booking> findByShowtimeId(Integer showtimeId);

       List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime dateTime);

       // Mới thêm
       boolean existsByShowtimeIdAndStatus(Integer showtimeId, com.cinema.enums.BookingStatus status);

       @Query("SELECT new com.cinema.dto.MovieBookingStatDto(m.id, m.title, COUNT(b), COALESCE(SUM(b.totalPrice), 0)) "
                     +
                     "FROM Movie m " +
                     "LEFT JOIN Showtime s ON s.movie = m " +
                     "LEFT JOIN Booking b ON b.showtime = s AND b.status = :status " +
                     "AND b.createdAt >= :startDate AND b.createdAt <= :endDate " +
                     "WHERE (:movieId IS NULL OR m.id = :movieId) " +
                     "GROUP BY m.id, m.title " +
                     // "ORDER BY COUNT(b) DESC")
                     "ORDER BY SUM(b.totalPrice) DESC")
       Page<MovieBookingStatDto> getMovieBookingStats(
                     @Param("status") BookingStatus status,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("movieId") Integer movieId,
                     Pageable pageable);
}
