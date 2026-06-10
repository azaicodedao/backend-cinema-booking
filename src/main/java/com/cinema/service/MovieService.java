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

    /**
     * Lấy tất cả các phim
     * 
     * @return danh sách các phim
     */
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movieMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các phim nổi bật
     * 
     * @return danh sách các phim nổi bật
     */
    public List<MovieItemDTO> getFeaturedMovies() {
        return movieRepository.findByIsFeatured(true).stream()
                .map(this::toItemDtoWithRating)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các phim đang chiếu
     * 
     * @return danh sách các phim đang chiếu
     */
    public List<MovieItemDTO> getShowingMovies() {
        return movieRepository.findByStatus(MovieStatus.SHOWING).stream()
                .map(this::toItemDtoWithRating)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các phim sắp chiếu
     * 
     * @return danh sách các phim sắp chiếu
     */
    public List<MovieItemDTO> getComingSoonMovies() {
        return movieRepository.findByStatus(MovieStatus.COMING).stream()
                .map(this::toItemDtoWithRating)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm phim
     * 
     * @param title   tiêu đề phim
     * @param genreId ID của thể loại
     * @param status  trạng thái
     * @return danh sách các phim tìm thấy
     */
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
                .map(this::toItemDtoWithRating)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin chi tiết phim
     * 
     * @param id ID của phim
     * @return thông tin chi tiết phim
     */
    public MovieDetailDTO getMovieDetail(Integer id) {
        Movie movie = findMovieById(id);
        MovieDetailDTO dto = movieMapper.toDetailDto(movie);
        movieRatingService.enrich(dto, id);
        return dto;
    }

    /**
     * Lấy thông tin phim theo ID
     * 
     * @param id ID của phim
     * @return thông tin phim
     */
    public MovieDto getMovieById(Integer id) {
        return movieMapper.toDto(findMovieById(id));
    }

    /**
     * Tạo phim mới
     * 
     * @param movieDto thông tin phim
     * @return phim đã tạo
     */
    @Transactional
    public MovieDto createMovie(MovieDto movieDto) {
        Movie movie = movieMapper.toEntity(movieDto);
        movie.setStatus(movieDto.getStatus() != null ? MovieStatus.valueOf(movieDto.getStatus()) : MovieStatus.SHOWING);
        movie.setIsFeatured(movieDto.getIsFeatured() != null ? movieDto.getIsFeatured() : false);
        movie.setPosterUrl(movieDto.getPosterUrl());
        applyGenres(movie, movieDto);

        Movie saved = movieRepository.save(movie);
        return movieMapper.toDto(saved);
    }

    /**
     * Sửa đổi phim
     * 
     * @param id       ID của phim
     * @param movieDto thông tin phim
     * @return phim đã sửa
     */
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
        String newPosterUrl = saved.getPosterUrl();
        if (oldPosterUrl != null && !oldPosterUrl.equals(newPosterUrl)) {
            moviePosterService.cleanupOldPosterAfterCommit(oldPosterUrl);
        }

        return movieMapper.toDto(saved);
    }

    /**
     * Xóa phim
     * 
     * @param id ID của phim
     */
    @Transactional
    public void deleteMovie(Integer id) {
        Movie movie = findMovieById(id);
        if (showtimeRepository.existsByMovieIdAndStartTimeAfter(id, LocalDateTime.now())) {
            throw new RuntimeException("Không thể xóa phim vì đang có suất chiếu trong tương lai");
        }

        movie.setStatus(MovieStatus.HIDDEN);
        movieRepository.save(movie);
    }

    /**
     * Tải lên poster phim
     * 
     * @param id   ID của phim
     * @param file file poster
     */
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

    /**
     * Lấy ảnh poster phim
     * 
     * @param id ID của phim
     * @return ảnh poster phim
     */
    public byte[] getMoviePoster(Integer id) {
        return new byte[0];
    }

    /**
     * Tìm phim theo ID
     * 
     * @param id ID của phim
     * @return phim
     * @throws RuntimeException nếu phim không tồn tại
     */
    private Movie findMovieById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    /**
     * Gán thể loại cho phim
     * 
     * @param movie    phim
     * @param movieDto thông tin phim
     */
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
        if (movieDto.getPosterUrl() == null) {
            return;
        }

        String posterUrl = movieDto.getPosterUrl().isBlank() ? null : movieDto.getPosterUrl();
        movie.setPosterUrl(posterUrl);
    }

    private MovieItemDTO toItemDtoWithRating(Movie movie) {
        MovieItemDTO dto = movieMapper.toItemDto(movie);
        movieRatingService.enrich(dto, movie.getId());
        return dto;
    }
}
