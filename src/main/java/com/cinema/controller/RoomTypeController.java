package com.cinema.controller;

import com.cinema.dto.RoomTypeDto;
import com.cinema.dto.response.ApiResponse;
import com.cinema.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/room-types")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomTypeDto>>> getAllRoomTypes() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách loại phòng thành công", roomTypeService.getAllRoomTypes()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomTypeDto>> createRoomType(@RequestBody RoomTypeDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thêm loại phòng thành công", roomTypeService.createRoomType(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomTypeDto>> updateRoomType(@PathVariable Integer id, @RequestBody RoomTypeDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật loại phòng thành công", roomTypeService.updateRoomType(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoomType(@PathVariable Integer id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa loại phòng thành công", null));
    }
}
