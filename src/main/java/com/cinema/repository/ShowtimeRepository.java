package com.cinema.repository;

import com.cinema.entity.Showtime;
import com.cinema.enums.ShowtimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

        List<Showtime> findByMovieId(Integer movieId);

        List<Showtime> findByShowDateIsNull();

        boolean existsByRoomId(Integer roomId);

        /**
         * Lấy tất cả suất chiếu của một ngày cụ thể, sắp xếp theo giờ bắt đầu tăng dần
         * dùng cho hiển thị lịch chiếu theo ngày.
         */
        List<Showtime> findByShowDateOrderByStartTimeAsc(LocalDate showDate);

        /**
         * Lấy tất cả các suất chiếu từ một ngày nhất định trở đi dùng cho bộ lọc suất
         * chiếu
         */
        List<Showtime> findByShowDateGreaterThanEqualOrderByStartTimeAsc(LocalDate startDate);

        /**
         * Lấy tất cả các suất chiếu có trạng thái OPEN và thời gian bắt đầu sau một
         * thời điểm nhất định dùng cho bộ lọc suất chiếu
         */
        List<Showtime> findByStatusAndStartTimeAfterOrderByStartTimeAsc(
                        ShowtimeStatus status,
                        LocalDateTime startTime);

        /**
         * Lấy tất cả các suất chiếu có trạng thái OPEN và thời gian bắt đầu sau một
         * thời điểm nhất định và ngày bắt đầu sau một ngày nhất định dùng cho bộ lọc
         * suất chiếu
         */
        List<Showtime> findByStatusAndShowDateAndStartTimeAfterOrderByStartTimeAsc(
                        ShowtimeStatus status,
                        LocalDate showDate,
                        LocalDateTime startTime);

        /**
         * Đếm số ghế còn trống của một suất chiếu dựa trên tổng ghế của phòng
         * trừ đi số ghế đã được book (trạng thái CONFIRMED).
         */
        @Query("""
                            SELECT (r.totalRows * r.totalCols) - COUNT(b.id)
                            FROM Showtime s
                            JOIN s.room r
                            LEFT JOIN Booking b ON b.showtime.id = s.id AND b.status = 'CONFIRMED'
                            WHERE s.id = :showtimeId
                            GROUP BY r.totalRows, r.totalCols
                        """)
        Integer countAvailableSeats(@Param("showtimeId") Integer showtimeId);

        boolean existsByMovieIdAndStartTimeAfter(Integer movieId, LocalDateTime now);

        @Query("""
                        SELECT COUNT(s) > 0 FROM Showtime s
                        WHERE s.room.id = :roomId
                        AND s.startTime < :endTime
                        AND s.endTime > :startTime
                        AND s.status != 'CLOSED'
                        """)
        boolean hasConflict(
                        @Param("roomId") Integer roomId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Query("""
                        SELECT COUNT(s) > 0 FROM Showtime s
                        WHERE s.room.id = :roomId
                        AND s.id != :showtimeId
                        AND s.startTime < :endTime
                        AND s.endTime > :startTime
                        AND s.status != 'CLOSED'
                        """)
        boolean hasConflictExcludeId(
                        @Param("roomId") Integer roomId,
                        @Param("showtimeId") Integer showtimeId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Query("""
                        SELECT new com.cinema.dto.ShowtimeStatDto(
                            s.id,
                            m.title,
                            r.name,
                            s.startTime,
                            CAST((r.totalRows * r.totalCols) AS long),
                            (SELECT COUNT(t) FROM Ticket t JOIN t.booking b WHERE b.showtime.id = s.id AND b.status = 'CONFIRMED'),
                            COALESCE((SELECT SUM(b.totalPrice) FROM Booking b WHERE b.showtime.id = s.id AND b.status = 'CONFIRMED'), 0)
                        )
                        FROM Showtime s
                        JOIN s.movie m
                        JOIN s.room r
                        WHERE s.endTime < :now
                        AND (:movieId IS NULL OR m.id = :movieId)
                        AND (:roomId IS NULL OR r.id = :roomId)
                        AND (:startDate IS NULL OR s.startTime >= :startDate)
                        AND (:endDate IS NULL OR s.startTime <= :endDate)
                        """)
        Page<com.cinema.dto.ShowtimeStatDto> getShowtimeStats(
                        @Param("now") LocalDateTime now,
                        @Param("movieId") Integer movieId,
                        @Param("roomId") Integer roomId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        @Query("SELECT s.id FROM Showtime s WHERE s.status = com.cinema.enums.ShowtimeStatus.OPEN AND s.endTime <= :now")
        List<Integer> findExpiredShowtimeIds(@Param("now") LocalDateTime now);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("""
                            UPDATE Showtime s
                            SET s.status = com.cinema.enums.ShowtimeStatus.CLOSED
                            WHERE s.id IN :ids
                        """)
        int closeShowtimesByIds(@Param("ids") List<Integer> ids);
}
