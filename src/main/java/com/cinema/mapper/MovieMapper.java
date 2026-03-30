package com.cinema.mapper;

import com.cinema.dto.MovieDto;
import com.cinema.entity.Movie;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {GenreMapper.class})
public interface MovieMapper {

    @Mapping(target = "status", expression = "java(movie.getStatus() != null ? movie.getStatus().name() : null)")
    @Mapping(target = "posterUrl", ignore = true)
    MovieDto toDto(Movie movie);

    @Mapping(target = "status", expression = "java(movieDto.getStatus() != null ? com.cinema.enums.MovieStatus.valueOf(movieDto.getStatus()) : null)")
    @Mapping(target = "posterUrl", ignore = true)
    Movie toEntity(MovieDto movieDto);

    @Mapping(target = "status", expression = "java(movieDto.getStatus() != null ? com.cinema.enums.MovieStatus.valueOf(movieDto.getStatus()) : null)")
    @Mapping(target = "posterUrl", ignore = true)
    void updateEntity(MovieDto movieDto, @MappingTarget Movie movie);
}
