package com.cinema.service;

import com.cinema.dto.RoomDto;
import com.cinema.dto.SeatDto;
import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import com.cinema.mapper.RoomMapper;
import com.cinema.mapper.SeatMapper;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.RoomTypeRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.SeatTypeRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.entity.SeatType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomService {

    RoomRepository roomRepository;
    RoomTypeRepository roomTypeRepository;
    SeatRepository seatRepository;
    SeatTypeRepository seatTypeRepository;
    RoomMapper roomMapper;
    SeatMapper seatMapper;
    ShowtimeRepository showtimeRepository;

    /**
     * Lấy tất cả các phòng
     * 
     * @return danh sách các phòng
     */
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các ghế trong một phòng
     * 
     * @param roomId ID của phòng
     * @return danh sách các ghế
     */
    public List<SeatDto> getRoomSeats(Integer roomId) {
        return seatRepository.findByRoomId(roomId).stream()
                .map(seatMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật loại ghế trong phòng
     * 
     * @param roomId   ID của phòng
     * @param seatDtos Danh sách ghế cần cập nhật
     * @return Danh sách ghế đã cập nhật
     */
    @Transactional
    public List<SeatDto> updateRoomSeats(Integer roomId, List<SeatDto> seatDtos) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));

        if (showtimeRepository.existsActiveShowtimesByRoomId(roomId, java.time.LocalDateTime.now())) {
            throw new RuntimeException("Không thể thay đổi loại ghế khi phòng đang có suất chiếu được lên lịch.");
        }

        for (SeatDto dto : seatDtos) {
            Seat seat = seatRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Seat not found"));
            if (!seat.getRoom().getId().equals(roomId))
                continue;

            String typeName = dto.getTypeName();
            if (typeName == null || typeName.equalsIgnoreCase("NORMAL")) {
                SeatType normalType = seatTypeRepository.findByName("NORMAL").orElse(null);
                seat.setSeatType(normalType);
            } else {
                SeatType type = seatTypeRepository.findByName(typeName)
                        .orElseGet(() -> {
                            SeatType newType = new SeatType();
                            newType.setName(typeName);
                            newType.setSurcharge(java.math.BigDecimal.valueOf(20000));
                            return seatTypeRepository.save(newType);
                        });
                seat.setSeatType(type);
            }
            seatRepository.save(seat);
        }

        return getRoomSeats(roomId);
    }

    /**
     * Tạo phòng mới
     * 
     * @param roomDto Thông tin phòng
     * @return Phòng đã tạo
     */
    @Transactional
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = roomMapper.toEntity(roomDto);

        // Gán loại phòng (RoomType) từ DB
        if (roomDto.getType() != null) {
            room.setRoomType(roomTypeRepository.findByName(roomDto.getType())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng: " + roomDto.getType())));
        }

        // Thiết lập trạng thái mặc định là ACTIVE
        room.setStatus(com.cinema.enums.RoomStatus.ACTIVE);

        if (roomDto.getTotalRows() != null)
            room.setTotalRows(roomDto.getTotalRows());
        if (roomDto.getTotalCols() != null)
            room.setTotalCols(roomDto.getTotalCols());

        Room savedRoom = roomRepository.save(room);

        for (int i = 0; i < savedRoom.getTotalRows(); i++) {
            char rowChar = (char) ('A' + i);
            for (int j = 1; j <= savedRoom.getTotalCols(); j++) {
                Seat seat = new Seat();
                seat.setRoom(savedRoom);
                seat.setRowLabel(String.valueOf(rowChar));
                seat.setColNumber(j);
                seatRepository.save(seat);
            }
        }

        return roomMapper.toDto(savedRoom);
    }

    /**
     * Sửa đổi phòng
     * 
     * @param id      ID của phòng
     * @param roomDto Thông tin phòng
     * @return Phòng đã sửa
     */
    @Transactional
    public RoomDto updateRoom(Integer id, RoomDto roomDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + id));
        if (roomDto.getName() != null)
            room.setName(roomDto.getName());
        if (roomDto.getType() != null)
            room.setRoomType(roomTypeRepository.findByName(roomDto.getType())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng: " + roomDto.getType())));

        if (roomDto.getStatus() != null) {
            room.setStatus(com.cinema.enums.RoomStatus.valueOf(roomDto.getStatus()));
        }

        if (roomDto.getTotalRows() != null && roomDto.getTotalCols() != null) {
            boolean layoutChanged = !roomDto.getTotalRows().equals(room.getTotalRows()) ||
                    !roomDto.getTotalCols().equals(room.getTotalCols());
            if (layoutChanged) {
                if (showtimeRepository.existsByRoomId(id)) {
                    throw new RuntimeException(
                            "Không thể thay đổi cấu hình ghế khi phòng đang có suất chiếu được lên lịch.");
                }

                room.setTotalRows(roomDto.getTotalRows());
                room.setTotalCols(roomDto.getTotalCols());

                seatRepository.deleteByRoomId(id);

                for (int i = 0; i < room.getTotalRows(); i++) {
                    char rowChar = (char) ('A' + i);
                    for (int j = 1; j <= room.getTotalCols(); j++) {
                        Seat seat = new Seat();
                        seat.setRoom(room);
                        seat.setRowLabel(String.valueOf(rowChar));
                        seat.setColNumber(j);
                        seatRepository.save(seat);
                    }
                }
            }
        }

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toDto(savedRoom);
    }

    // Hiển thị
    public RoomDto getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return roomMapper.toDto(room);
    }

    // Xóa phòng
    @Transactional
    public void deleteRoom(Integer id) {
        if (showtimeRepository.existsByRoomId(id)) {
            throw new RuntimeException("Không thể xoá phòng đang có suất chiếu. Vui lòng xoá suất chiếu trước.");
        }
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + id));

        // Xoá toàn bộ ghế thuộc phòng trước để tránh lỗi ràng buộc khoá ngoại
        seatRepository.deleteByRoomId(id);

        roomRepository.delete(room);
    }
}
