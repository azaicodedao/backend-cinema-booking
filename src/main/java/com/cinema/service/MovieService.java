package com.cinema.service;

import com.cinema.dto.GenreDto;
import com.cinema.dto.MovieDetailDTO;
import com.cinema.dto.MovieDto;
import com.cinema.dto.MovieItemDTO;
import com.cinema.entity.Genre;
import com.cinema.entity.Movie;
import com.cinema.enums.MovieStatus;
import com.cinema.mapper.MovieMapper;
import com.cinema.repository.GenreRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ShowtimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {

    MovieRepository movieRepository;
    GenreRepository genreRepository;
    MovieMapper movieMapper;
    ShowtimeRepository showtimeRepository;
    MoviePosterService moviePosterService;
    MovieRatingService movieRatingService;

    // Chuyển đổi sang MovieItemDTO với rating
    private MovieItemDTO toItemDtoWithRating(Movie movie) {
        MovieItemDTO dto = movieMapper.toItemDto(movie);
        movieRatingService.enrich(dto, movie.getId());
        return dto;
    }

    // Lấy tất cả các phim
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movie -> movieMapper.toDto(movie))
                .collect(Collectors.toList());
    }

    // Lấy danh sách các phim nổi bật
    public List<MovieItemDTO> getFeaturedMovies() {
        return movieRepository.findByIsFeatured(true).stream()
                .map(movie -> this.toItemDtoWithRating(movie))
                .collect(Collectors.toList());
    }

    // Lấy danh sách các phim đang chiếu
    public List<MovieItemDTO> getShowingMovies() {
        return movieRepository.findByStatus(MovieStatus.SHOWING).stream()
                .map(movie -> this.toItemDtoWithRating(movie))
                .collect(Collectors.toList());
    }

    // Lấy danh sách các phim sắp chiếu
    public List<MovieItemDTO> getComingSoonMovies() {
        return movieRepository.findByStatus(MovieStatus.COMING).stream()
                .map(movie -> this.toItemDtoWithRating(movie))
                .collect(Collectors.toList());
    }

    // Tìm kiếm phim
    public List<MovieItemDTO> searchMovies(String title, Integer genreId, MovieStatus status) {
        List<Movie> movies;
        if (status != null) {
            if (genreId != null && title != null && !title.isEmpty()) {
                movies = movieRepository.findByStatusAndGenresIdAndTitleContainingIgnoreCase(status, genreId, title);
            } else if (genreId != null) {
                movies = movieRepository.findByStatusAndGenresId(status, genreId);
            } else if (title != null && !title.isEmpty()) {
                movies = movieRepository.findByStatus(status).stream()
                        .filter(movie -> movie.getTitle().toLowerCase().contains(title.toLowerCase()))
                        .collect(Collectors.toList());
            } else {
                movies = movieRepository.findByStatus(status);
            }
        } else {
            if (genreId != null && title != null && !title.isEmpty()) {
                movies = movieRepository.findByGenresIdAndTitleContainingIgnoreCase(genreId, title);
            } else if (genreId != null) {
                movies = movieRepository.findByGenresId(genreId);
            } else if (title != null && !title.isEmpty()) {
                movies = movieRepository.findByTitleContainingIgnoreCase(title);
            } else {
                movies = movieRepository.findAll();
            }
        }
        return movies.stream()
                .map(movie -> this.toItemDtoWithRating(movie))
                .collect(Collectors.toList());
    }

    // Tìm phim theo ID
    private Movie findMovieById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    // Lấy thông tin cơ bản của phim theo ID
    public MovieDto getMovieById(Integer id) {
        return movieMapper.toDto(findMovieById(id));
    }

    // Lấy thông tin chi tiết phim
    public MovieDetailDTO getMovieDetail(Integer id) {
        Movie movie = findMovieById(id);
        MovieDetailDTO dto = movieMapper.toDetailDto(movie);
        movieRatingService.enrich(dto, id);
        return dto;
    }

    // Gán thể loại cho phim
    private void applyGenres(Movie movie, MovieDto movieDto) {
        if (movieDto.getGenres() == null) {
            return;
        }
        List<Genre> genres = genreRepository.findAllById(
                movieDto.getGenres().stream()
                        .map(GenreDto::getId)
                        .collect(Collectors.toList()));
        movie.setGenres(genres);
    }

    private void applyPosterUpdate(Movie movie, MovieDto movieDto) {
        if (movieDto.getPosterUrl() == null)
            return;

        String posterUrl = movieDto.getPosterUrl().isBlank() ? null : movieDto.getPosterUrl();
        movie.setPosterUrl(posterUrl);
    }

    // Tạo phim mới
    @Transactional
    public MovieDto createMovie(MovieDto movieDto) {
        Movie movie = movieMapper.toEntity(movieDto);
        movie.setStatus(
                movieDto.getStatus() != null
                        ? MovieStatus.valueOf(movieDto.getStatus())
                        : MovieStatus.SHOWING);
        movie.setIsFeatured(movieDto.getIsFeatured() != null ? movieDto.getIsFeatured() : false);
        movie.setPosterUrl(movieDto.getPosterUrl());
        applyGenres(movie, movieDto);

        Movie saved = movieRepository.save(movie);
        return movieMapper.toDto(saved);
    }

    // Sửa đổi phim
    @Transactional
    public MovieDto updateMovie(Integer id, MovieDto movieDto) {
        Movie movie = findMovieById(id);
        String oldPosterUrl = movie.getPosterUrl();

        movieMapper.updateEntity(movieDto, movie);
        if (movieDto.getIsFeatured() != null) {
            movie.setIsFeatured(movieDto.getIsFeatured());
        }
        applyGenres(movie, movieDto);
        applyPosterUpdate(movie, movieDto);

        Movie saved = movieRepository.save(movie);
        String newPosterUrl = saved.getPosterUrl(); // Lấy URL poster mới sau khi lưu
        if (oldPosterUrl != null && !oldPosterUrl.equals(newPosterUrl)) { // Xóa URL poster cũ nếu có trong DB
            moviePosterService.cleanupOldPosterAfterCommit(oldPosterUrl);
        }

        return movieMapper.toDto(saved);
    }

    // Xóa phim
    @Transactional
    public void deleteMovie(Integer id) {
        Movie movie = findMovieById(id);
        if (showtimeRepository.existsByMovieIdAndStartTimeAfter(id, LocalDateTime.now())) {
            throw new RuntimeException("Không thể xóa phim vì đang có suất chiếu trong tương lai");
        }

        movie.setStatus(MovieStatus.HIDDEN);
        movieRepository.save(movie);
    }

    // Tải poster lên phim
    @Transactional
    public void uploadMoviePoster(Integer id, MultipartFile file) {
        Movie movie = findMovieById(id);
        String oldPosterUrl = movie.getPosterUrl();
        try {
            String url = moviePosterService.uploadPoster(file);
            movie.setPosterUrl(url);
            movieRepository.save(movie);
            moviePosterService.cleanupOldPosterAfterCommit(oldPosterUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    // Lấy ảnh poster phim
    public byte[] getMoviePoster(Integer id) {
        return new byte[0];
    }

}
