package com.cinema.mapper;

import com.cinema.dto.request.BookingRequestDto;
import com.cinema.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking toEntity(BookingRequestDto bookingRequestDto);
}
