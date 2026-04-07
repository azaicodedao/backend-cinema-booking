package com.cinema.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDto {
    private Integer seatId;
    private String rowLabel;
    private Integer colNumber;
    private String seatType;
    private BigDecimal price;
    private String status; // AVAILABLE, HOLDING, BOOKED
    private Integer holdByUserId;
    private LocalDateTime expiredAt;
}
