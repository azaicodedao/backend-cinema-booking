package com.cinema.service;

import com.cinema.dto.MovieScheduleDto;
import com.cinema.dto.ShowtimeDto;
import com.cinema.dto.ShowtimeSnapshotDto;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.mapper.ShowtimeMapper;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ReviewRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.repository.BookingRepository;
import com.cinema.enums.BookingStatus;
import com.cinema.enums.ShowtimeStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowtimeService {

    ShowtimeRepository showtimeRepository;
    MovieRepository movieRepository;
    RoomRepository roomRepository;
    ShowtimeMapper showtimeMapper;
    ReviewRepository reviewRepository;
    BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<ShowtimeDto> getAllShowtimes() {
        return showtimeRepository.findAll().stream()
                .map(showtimeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeDto> getShowtimesByMovie(Integer movieId) {
        return showtimeRepository.findByMovieId(movieId).stream()
                .map(st -> {
                    ShowtimeDto dto = showtimeMapper.toDto(st);
                    enrichShowtimeDto(dto, st);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieScheduleDto> getScheduleByDate(LocalDate targetDate) {
        // 1. Tự động sửa các bản ghi cũ bị thiếu showDate (chỉ quét các bản ghi bị
        // null)
        List<Showtime> missingDateShowtimes = showtimeRepository.findByShowDateIsNull();
        if (!missingDateShowtimes.isEmpty()) {
            for (Showtime st : missingDateShowtimes) {
                if (st.getStartTime() != null) {
                    st.setShowDate(st.getStartTime().toLocalDate());
                    showtimeRepository.save(st);
                }
            }
        }

        // 2. Lấy tất cả showtimes của ngày, sắp xếp theo giờ tăng dần
        List<Showtime> showtimes = showtimeRepository
                .findByShowDateOrderByStartTimeAsc(targetDate != null ? targetDate : LocalDate.now());

        if (showtimes.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Nhóm theo Movie (dùng LinkedHashMap để giữ thứ tự insert)
        Map<Movie, List<Showtime>> grouped = new LinkedHashMap<>();
        for (Showtime st : showtimes) {
            Movie movie = st.getMovie();
            grouped.computeIfAbsent(movie, k -> new ArrayList<>()).add(st);
        }

        // 3. Chuyển đổi sang MovieScheduleDto
        return grouped.entrySet().stream()
                .map(entry -> buildMovieScheduleDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Tổng hợp thông tin phim và danh sách suất chiếu thành MovieScheduleDto.
     */
    private MovieScheduleDto buildMovieScheduleDto(Movie movie, List<Showtime> showtimes) {
        // Lấy tên thể loại
        String genres = "";
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            genres = movie.getGenres().stream()
                    .map(g -> g.getName())
                    .collect(Collectors.joining(", "));
        }

        // Tính điểm rating trung bình từ bảng reviews
        Double rating = null;
        try {
            Double avg = reviewRepository.getAverageRatingByMovieId(movie.getId());
            if (avg != null) {
                // Làm tròn 1 chữ số thập phân
                rating = Math.round(avg * 10.0) / 10.0;
            }
        } catch (Exception ignored) {
            // Nếu chưa có dữ liệu review, để null
        }

        // Build danh sách ShowtimeSnapshotDto
        List<ShowtimeSnapshotDto> snapshots = showtimes.stream()
                .map(st -> buildShowtimeSnapshot(st))
                .collect(Collectors.toList());

        return MovieScheduleDto.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .genres(genres)
                .ageRating(movie.getAgeRating())
                .rating(rating)
                .duration(movie.getDuration())
                .showtimes(snapshots)
                .build();
    }

    /**
     * Chuyển đổi entity Showtime thành ShowtimeSnapshotDto.
     * Tổng hợp chuỗi "formatAndRoom" từ loại phòng và tên phòng.
     */
    private ShowtimeSnapshotDto buildShowtimeSnapshot(Showtime st) {
        String timeString = "";
        if (st.getStartTime() != null) {
            timeString = st.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        // Tổng hợp "IMAX · Phòng 1" hoặc "2D · Phòng 3"
        String formatAndRoom = buildFormatAndRoom(st.getRoom());

        // Số ghế còn trống
        Integer availableSeats = null;
        try {
            availableSeats = showtimeRepository.countAvailableSeats(st.getId());
        } catch (Exception ignored) {
            // Nếu chưa có dữ liệu booking, để null
        }

        String status = "AVAILABLE";
        if (availableSeats != null && availableSeats <= 0) {
            status = "FULL";
        } else if (st.getStatus() != null) {
            status = st.getStatus().name();
        }

        return ShowtimeSnapshotDto.builder()
                .id(st.getId())
                .startTime(st.getStartTime())
                .timeString(timeString)
                .formatAndRoom(formatAndRoom)
                .status(status)
                .availableSeats(availableSeats)
                .build();
    }

    /**
     * Tạo chuỗi mô tả định dạng chiếu và phòng, VD: "IMAX · Phòng 1", "2D · Phòng
     * 3".
     */
    private String buildFormatAndRoom(Room room) {
        if (room == null)
            return "";
        String format = "2D";
        if (room.getRoomType() != null) {
            format = room.getRoomType().getName();
        }
        String roomName = room.getName() != null ? room.getName() : "";
        return format + " · " + roomName;
    }

    /**
     * Làm giàu dữ liệu cho ShowtimeDto từ entity Showtime.
     */
    private void enrichShowtimeDto(ShowtimeDto dto, Showtime st) {
        if (st.getStartTime() != null) {
            dto.setTimeString(st.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            // Đảm bảo showDate luôn có giá trị trong DTO
            if (dto.getShowDate() == null) {
                dto.setShowDate(st.getStartTime().toLocalDate());
            }
        }

        dto.setFormatAndRoom(buildFormatAndRoom(st.getRoom()));

        Integer availableSeats = null;
        try {
            availableSeats = showtimeRepository.countAvailableSeats(st.getId());
            dto.setAvailableSeats(availableSeats);
        } catch (Exception ignored) {
        }

        if (availableSeats != null && availableSeats <= 0) {
            dto.setStatus("FULL");
        } else if (st.getStatus() != null) {
            dto.setStatus(st.getStatus().name());
        } else {
            dto.setStatus("AVAILABLE");
        }
    }

    // Mới thêm
    @Transactional
    public ShowtimeDto createShowtime(ShowtimeDto showtimeDto) {
        // 1. Fetch related entities
        Movie movie = movieRepository.findById(showtimeDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        Room room = roomRepository.findById(showtimeDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Mới thêm
        // Tính toán giờ kết thúc: giờ bắt đầu + thời lượng phim + 15p buffer
        java.time.LocalDateTime endTime = showtimeDto.getStartTime()
                .plusMinutes(movie.getDuration())
                .plusMinutes(15);

        // Kiểm tra xung đột lịch chiếu
        if (showtimeRepository.hasConflict(room.getId(), showtimeDto.getStartTime(), endTime)) {
            throw new IllegalArgumentException("Phòng chiếu đã có lịch trong khung giờ này");
        }

        // 2. Map DTO to Entity (Tự động gán showDate thông qua @AfterMapping trong
        // Mapper)
        Showtime showtime = showtimeMapper.toEntity(showtimeDto);
        showtime.setMovie(movie);
        showtime.setRoom(room);

        // Mới thêm
        showtime.setEndTime(endTime);
        showtime.setStatus(ShowtimeStatus.OPEN);

        if (showtimeDto.getBasePrice() != null)
            showtime.setBasePrice(BigDecimal.valueOf(showtimeDto.getBasePrice()));

        // 3. Save and return DTO
        Showtime saved = showtimeRepository.save(showtime);
        return showtimeMapper.toDto(saved);
    }

    // Mới thêm

    @Transactional
    public ShowtimeDto updateShowtime(Integer id, ShowtimeDto showtimeDto) {
        Showtime existingShowtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        Movie movie = movieRepository.findById(showtimeDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        Room newRoom = roomRepository.findById(showtimeDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        boolean hasConfirmedTickets = bookingRepository.existsByShowtimeIdAndStatus(id, BookingStatus.CONFIRMED);

        if (hasConfirmedTickets) {
            Room oldRoom = existingShowtime.getRoom();
            int oldCapacity = oldRoom.getTotalRows() * oldRoom.getTotalCols();
            int newCapacity = newRoom.getTotalRows() * newRoom.getTotalCols();
            if (newCapacity < oldCapacity) {
                throw new IllegalArgumentException(
                        "Suất chiếu đã có vé CONFIRMED chỉ có thể sửa phòng chiếu cùng sức chứa hoặc lớn hơn.");
            }
        }

        // Tính lại giờ kết thúc
        java.time.LocalDateTime endTime = showtimeDto.getStartTime()
                .plusMinutes(movie.getDuration())
                .plusMinutes(15);

        // Kiểm tra xung đột (bỏ qua id hiện tại)
        if (showtimeRepository.hasConflictExcludeId(newRoom.getId(), id, showtimeDto.getStartTime(), endTime)) {
            throw new IllegalArgumentException("Phòng chiếu đã có lịch trong khung giờ này");
        }

        existingShowtime.setMovie(movie);
        existingShowtime.setRoom(newRoom);
        existingShowtime.setStartTime(showtimeDto.getStartTime());
        existingShowtime.setEndTime(endTime);
        existingShowtime.setShowDate(showtimeDto.getStartTime().toLocalDate());
        if (showtimeDto.getBasePrice() != null) {
            existingShowtime.setBasePrice(BigDecimal.valueOf(showtimeDto.getBasePrice()));
        }

        Showtime saved = showtimeRepository.save(existingShowtime);
        return showtimeMapper.toDto(saved);
    }

    @Transactional
    public void deleteShowtime(Integer id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        if (bookingRepository.existsByShowtimeIdAndStatus(id, BookingStatus.CONFIRMED)) {
            throw new IllegalArgumentException(
                    "Không thể xoá suất chiếu đã có khách đặt vé. Vui lòng huỷ các vé liên quan trước.");
        }

        showtime.setStatus(ShowtimeStatus.CLOSED);
        showtimeRepository.save(showtime);
    }
}
