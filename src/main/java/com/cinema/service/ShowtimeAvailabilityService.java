package com.cinema.service;

import com.cinema.entity.Showtime;
import com.cinema.enums.ShowtimeStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShowtimeAvailabilityService {

    // Kiểm tra xem suất chiếu có khả dụng để đặt vé không
    public boolean isBookable(Showtime showtime) {
        return showtime != null
                && showtime.getStatus() == ShowtimeStatus.OPEN
                && showtime.getStartTime() != null
                && showtime.getStartTime().isAfter(LocalDateTime.now());
    }

    public void validateBookable(Showtime showtime) {
        if (!isBookable(showtime)) {
            throw new IllegalArgumentException("Suat chieu nay khong con kha dung.");
        }
    }
}
