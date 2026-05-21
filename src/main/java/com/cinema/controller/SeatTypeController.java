package com.cinema.controller;

import com.cinema.dto.SeatTypeDto;
import com.cinema.dto.response.ApiResponse;
import com.cinema.service.SeatTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seat-types")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SeatTypeController {

    private final SeatTypeService seatTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeatTypeDto>>> getAllSeatTypes() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách loại ghế thành công", seatTypeService.getAllSeatTypes()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeatTypeDto>> createSeatType(@RequestBody SeatTypeDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thêm loại ghế thành công", seatTypeService.createSeatType(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeatTypeDto>> updateSeatType(@PathVariable Integer id, @RequestBody SeatTypeDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật loại ghế thành công", seatTypeService.updateSeatType(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSeatType(@PathVariable Integer id) {
        seatTypeService.deleteSeatType(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa loại ghế thành công", null));
    }
}
