package com.cinema.task;
 
 import com.cinema.service.SeatHoldingService;
 import lombok.AccessLevel;
 import lombok.RequiredArgsConstructor;
 import lombok.experimental.FieldDefaults;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
 
 @Component
 @RequiredArgsConstructor
 @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
 @Slf4j
 public class SeatCleanupTask {
 
     SeatHoldingService seatHoldingService;
 
     /**
      * Chạy mỗi 30 giây (30,000 milis) để giải phóng các ghế đã hết hạn giữ.
      */
     @Scheduled(fixedRate = 30000)
     public void cleanupExpiredHolds() {
         log.debug("Checking for expired seat holds...");
         seatHoldingService.releaseExpiredHolds();
     }
 }
