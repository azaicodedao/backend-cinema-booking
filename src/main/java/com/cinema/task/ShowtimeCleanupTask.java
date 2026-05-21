package com.cinema.task;

import com.cinema.repository.ShowtimeRepository;
import com.cinema.repository.TicketRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShowtimeCleanupTask {

    ShowtimeRepository showtimeRepository;
    TicketRepository ticketRepository;

    /**
     * Tự động quét và cập nhật trạng thái suất chiếu và vé đã kết thúc.
     * Chạy định kỳ mỗi 1 phút (60,000 milliseconds).
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredShowtimesAndTickets() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("Kiểm tra và dọn dẹp suất chiếu và vé đã kết thúc...");

        // 1. Chuyển trạng thái các vé chưa sử dụng (VALID) của tất cả các suất chiếu đã kết thúc sang USED
        int updatedTicketsCount = ticketRepository.markExpiredShowtimeTicketsAsUsed(now);
        if (updatedTicketsCount > 0) {
            log.info("Đã tự động chuyển {} vé chưa sử dụng sang trạng thái USED cho các suất chiếu đã kết thúc.", updatedTicketsCount);
        }

        // 2. Tìm các ID suất chiếu đã kết thúc nhưng vẫn đang OPEN
        List<Integer> expiredShowtimeIds = showtimeRepository.findExpiredShowtimeIds(now);

        if (expiredShowtimeIds != null && !expiredShowtimeIds.isEmpty()) {
            log.info("Tìm thấy {} suất chiếu đã hết thời gian chiếu cần đóng: {}", expiredShowtimeIds.size(), expiredShowtimeIds);

            // 3. Chuyển trạng thái các suất chiếu này sang CLOSED
            int closedShowtimesCount = showtimeRepository.closeShowtimesByIds(expiredShowtimeIds);
            if (closedShowtimesCount > 0) {
                log.info("Đã đóng {} suất chiếu đã hết thời gian chiếu.", closedShowtimesCount);
            }
        }
    }
}
