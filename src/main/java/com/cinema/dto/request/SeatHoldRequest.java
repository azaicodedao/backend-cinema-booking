package com.cinema.dto.request;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldRequest {
    private Integer showtimeId;
    private List<Integer> seatIds;
}
