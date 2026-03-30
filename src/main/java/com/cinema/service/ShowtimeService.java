package com.cinema.service;

import com.cinema.dto.ShowtimeDto;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.mapper.ShowtimeMapper;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.ShowtimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowtimeService {

    ShowtimeRepository showtimeRepository;
    MovieRepository movieRepository;
    RoomRepository roomRepository;
    ShowtimeMapper showtimeMapper;

    public List<ShowtimeDto> getAllShowtimes() {
        return showtimeRepository.findAll().stream()
                .map(showtimeMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ShowtimeDto> getShowtimesByMovie(Integer movieId) {
        return showtimeRepository.findByMovieId(movieId).stream()
                .map(showtimeMapper::toDto)
                .collect(Collectors.toList());
    }

    public ShowtimeDto createShowtime(ShowtimeDto showtimeDto) {
        Movie movie = movieRepository.findById(showtimeDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        Room room = roomRepository.findById(showtimeDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(showtimeDto.getStartTime());
        showtime.setEndTime(showtimeDto.getEndTime());
        if (showtimeDto.getPrice() != null) {
            showtime.setPrice(BigDecimal.valueOf(showtimeDto.getPrice()));
        }

        Showtime saved = showtimeRepository.save(showtime);
        return showtimeMapper.toDto(saved);
    }
}
