package com.cinema.mapper;

import com.cinema.dto.GenreDto;
import com.cinema.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    GenreDto toDto(Genre genre);
    Genre toEntity(GenreDto genreDto);
    void updateEntity(GenreDto genreDto, @MappingTarget Genre genre);
}
