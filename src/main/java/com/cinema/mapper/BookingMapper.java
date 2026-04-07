package com.cinema.mapper;

import com.cinema.dto.request.BookingRequestDto;
import com.cinema.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "showtime.id", source = "showtimeId")
    Booking toEntity(BookingRequestDto bookingRequestDto);
}
