package com.cinema.mapper;

import com.cinema.dto.ReviewDto;
import com.cinema.dto.ReviewUserDto;
import com.cinema.entity.Review;
import com.cinema.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "movie.id", target = "movieId")
    @Mapping(source = "movie.title", target = "movieTitle")
    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "username")
    @Mapping(source = "user", target = "user")
    ReviewDto toDto(Review review);

    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "avatarUrl", target = "avatarUrl")
    ReviewUserDto toReviewUser(User user);

    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toEntity(ReviewDto reviewDto);
}
