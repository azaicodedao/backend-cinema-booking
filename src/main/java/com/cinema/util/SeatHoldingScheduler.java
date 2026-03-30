package com.cinema.util;

import com.cinema.service.SeatHoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatHoldingScheduler {

    private final SeatHoldingService SeatHoldingService;

    @Scheduled(fixedRate = 30000)
    public void releaseExpiredHolds() {
        log.debug("Checking for expired seat holds...");
        SeatHoldingService.releaseExpiredHolds();
    }
}
