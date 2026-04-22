package com.cinema.mapper;

import com.cinema.dto.ShowtimeDto;
import com.cinema.entity.*;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ShowtimeMapper {

    @Mapping(source = "movie.id", target = "movieId")
    @Mapping(source = "movie.title", target = "movieTitle")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(target = "roomSurcharge", expression = "java(showtime.getRoom() != null && showtime.getRoom().getRoomType() != null ? (showtime.getRoom().getRoomType().getSurcharge() != null ? showtime.getRoom().getRoomType().getSurcharge().doubleValue() : 0.0) : 0.0)")
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(target = "timeString", ignore = true)
    @Mapping(target = "formatAndRoom", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    ShowtimeDto toDto(Showtime showtime);

    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? com.cinema.enums.ShowtimeStatus.valueOf(dto.getStatus()) : null)")
    Showtime toEntity(ShowtimeDto dto);

    @AfterMapping
    default void fillShowDate(@MappingTarget Showtime showtime, ShowtimeDto dto) {
        if (showtime.getShowDate() == null && dto.getStartTime() != null) {
            showtime.setShowDate(dto.getStartTime().toLocalDate());
        }
    }
}
