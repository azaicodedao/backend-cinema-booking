package com.cinema.service;

import com.cinema.dto.GenreDto;
import com.cinema.dto.MovieDto;
import com.cinema.entity.Genre;
import com.cinema.entity.Movie;
import com.cinema.mapper.MovieMapper;
import com.cinema.repository.GenreRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.enums.MovieStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {

    MovieRepository movieRepository;
    GenreRepository GenreRepository;
    MovieMapper movieMapper;

    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::toDtoWithPoster)
                .collect(Collectors.toList());
    }

    public MovieDto getMovieById(Integer id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return toDtoWithPoster(movie);
    }

    public MovieDto createMovie(MovieDto movieDto) {
        Movie movie = movieMapper.toEntity(movieDto);
        movie.setStatus(MovieStatus.SHOWING);

        if (movieDto.getGenres() != null) {
            List<Genre> Genres = GenreRepository.findAllById(
                    movieDto.getGenres().stream().map(GenreDto::getId).collect(Collectors.toList()));
            movie.setGenres(Genres);
        }

        Movie saved = movieRepository.save(movie);
        return movieMapper.toDto(saved);
    }

    public MovieDto updateMovie(Integer id, MovieDto movieDto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        movieMapper.updateEntity(movieDto, movie);

        if (movieDto.getGenres() != null) {
            List<Genre> Genres = GenreRepository.findAllById(
                    movieDto.getGenres().stream().map(GenreDto::getId).collect(Collectors.toList()));
            movie.setGenres(Genres);
        }

        Movie saved = movieRepository.save(movie);
        return movieMapper.toDto(saved);
    }

    public void deleteMovie(Integer id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setStatus(MovieStatus.ENDED); // map to ENDED if HIDDEN was used before
        movieRepository.save(movie);
    }

    private MovieDto toDtoWithPoster(Movie movie) {
        MovieDto dto = movieMapper.toDto(movie);
        if (movie.getPosterUrl() != null) {
            String posterUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/movies/")
                    .path(movie.getId().toString())
                    .path("/image")
                    .toUriString();
            dto.setPosterUrl(posterUrl);
        }
        return dto;
    }

    public void uploadMoviePoster(Integer id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        try {
            movie.setPosterUrl("/uploads/" + file.getOriginalFilename());
            movieRepository.save(movie);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public byte[] getMoviePoster(Integer id) {
        return new byte[0];
    }
}
