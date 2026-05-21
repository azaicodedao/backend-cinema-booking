package com.cinema.repository;

import com.cinema.entity.Booking;
import com.cinema.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
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

       boolean existsByShowtimeId(Integer showtimeId);

       boolean existsByShowtimeIdAndStatus(Integer showtimeId, com.cinema.enums.BookingStatus status);

       /**
        * Lấy danh sách thống kê lượt đặt vé & doanh thu theo từng phim
        * 
        * @param status
        * @param startDate
        * @param endDate
        * @param movieId
        * @param pageable
        * @return
        */
       @Query("SELECT new com.cinema.dto.MovieBookingStatDto(" +
                     "  m.id, " +
                     "  m.title, " +
                     "  (SELECT COUNT(t) FROM Ticket t JOIN t.booking b2 JOIN b2.showtime s2 WHERE s2.movie = m AND b2.status = :status AND b2.createdAt >= :startDate AND b2.createdAt <= :endDate), "
                     +
                     "  COALESCE(SUM(b.totalPrice), 0)" +
                     ") " +
                     "FROM Movie m " +
                     "LEFT JOIN Showtime s ON s.movie = m " +
                     "LEFT JOIN Booking b ON b.showtime = s AND b.status = :status " +
                     "AND b.createdAt >= :startDate AND b.createdAt <= :endDate " +
                     "WHERE (:movieId IS NULL OR m.id = :movieId) " +
                     "GROUP BY m.id, m.title " +
                     "ORDER BY SUM(b.totalPrice) DESC")
       Page<MovieBookingStatDto> getMovieBookingStats(
                     @Param("status") BookingStatus status,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("movieId") Integer movieId,
                     Pageable pageable);

       /**
        * Lấy tổng số lượt đặt vé & tổng doanh thu (toàn hệ thống)
        * 
        * @param status
        * @param startDate
        * @param endDate
        * @param movieId
        * @return
        */
       @Query("SELECT COUNT(b), COALESCE(SUM(b.totalPrice), 0) " +
                     "FROM Booking b " +
                     "JOIN b.showtime s " +
                     "JOIN s.movie m " +
                     "WHERE b.status = :status " +
                     "AND b.createdAt >= :startDate " +
                     "AND b.createdAt <= :endDate " +
                     "AND (:movieId IS NULL OR m.id = :movieId)")
       java.util.List<Object[]> getGlobalBookingStats(
                     @Param("status") BookingStatus status,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("movieId") Integer movieId);

       @Query("SELECT b.createdAt, b.totalPrice " +
              "FROM Booking b " +
              "WHERE b.status = :status " +
              "AND b.createdAt >= :startDate " +
              "AND b.createdAt <= :endDate " +
              "ORDER BY b.createdAt ASC")
       java.util.List<Object[]> getRawRevenueByDate(
                      @Param("status") BookingStatus status,
                      @Param("startDate") LocalDateTime startDate,
                      @Param("endDate") LocalDateTime endDate);
}
