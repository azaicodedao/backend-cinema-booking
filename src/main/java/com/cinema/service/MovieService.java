package com.cinema.service;

import com.cinema.dto.GenreDto;
import com.cinema.dto.MovieDetailDTO;
import com.cinema.dto.MovieDto;
import com.cinema.dto.MovieItemDTO;
import com.cinema.entity.Genre;
import com.cinema.entity.Movie;
import com.cinema.mapper.MovieMapper;
import com.cinema.repository.GenreRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ReviewRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.service.Cloudinary.CloudinaryService;
import com.cinema.enums.MovieStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {

    MovieRepository movieRepository;
    GenreRepository GenreRepository;
    ReviewRepository reviewRepository;
    MovieMapper movieMapper;
    CloudinaryService cloudinaryService;
    ShowtimeRepository showtimeRepository;

    /**
     * Lấy tất cả danh sách phim dưới dạng MovieDto.
     * 
     * @return Danh sách phim cơ bản.
     */
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::toDtoWithPoster)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các bộ phim được đánh dấu là nổi bật (isFeatured = true).
     * 
     * @return Danh sách MovieItemDTO cho các phim nổi bật.
     */
    public List<MovieItemDTO> getFeaturedMovies() {
        return movieRepository.findByIsFeatured(true).stream()
                .map(this::toItemDtoWithPoster)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các bộ phim đang chiếu.
     * 
     * @return Danh sách MovieItemDTO cho các phim đang chiếu.
     */
    public List<MovieItemDTO> getShowingMovies() {
        return movieRepository.findByStatus(MovieStatus.SHOWING).stream()
                .map(this::toItemDtoWithPoster)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các bộ phim sắp chiếu.
     * 
     * @return Danh sách MovieItemDTO cho các phim sắp chiếu.
     */
    public List<MovieItemDTO> getComingSoonMovies() {
        return movieRepository.findByStatus(MovieStatus.COMING).stream()
                .map(this::toItemDtoWithPoster)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm và lọc phim linh hoạt dựa trên tiêu đề, thể loại và trạng thái.
     * 
     * @param title   Tiêu đề phim (tùy chọn).
     * @param genreId Mã thể loại (tùy chọn).
     * @param status  Trạng thái phim (tùy chọn).
     * @return Danh sách MovieItemDTO phù hợp với tiêu chí lọc.
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
                        .filter(m -> m.getTitle().toLowerCase().contains(title.toLowerCase()))
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
        return movies.stream().map(this::toItemDtoWithPoster).collect(Collectors.toList());
    }

    /**
     * Lấy thông tin chi tiết đầy đủ của một bộ phim bao gồm thống kê đánh giá.
     * 
     * @param id ID của phim.
     * @return MovieDetailDTO chứa chi tiết phim và điểm đánh giá.
     */
    public MovieDetailDTO getMovieDetail(Integer id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        MovieDetailDTO dto = movieMapper.toDetailDto(movie);
        enrichDetailWithPoster(dto, movie);
        enrichDetailWithRating(dto, id);
        return dto;
    }

    /**
     * Lấy thông tin phim cơ bản theo ID.
     * 
     * @param id ID của phim.
     * @return MovieDto của phim.
     */
    public MovieDto getMovieById(Integer id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return toDtoWithPoster(movie);
    }

    /**
     * Tạo một bộ phim mới trong hệ thống.
     * 
     * @param movieDto Dữ liệu phim cần tạo.
     * @return MovieDto của phim đã tạo thành công.
     */
    @Transactional
    public MovieDto createMovie(MovieDto movieDto) {
        Movie movie = movieMapper.toEntity(movieDto);
        movie.setStatus(movieDto.getStatus() != null ? MovieStatus.valueOf(movieDto.getStatus()) : MovieStatus.SHOWING);
        movie.setIsFeatured(movieDto.getIsFeatured() != null ? movieDto.getIsFeatured() : false);

        if (movieDto.getGenres() != null) {
            List<Genre> Genres = GenreRepository.findAllById(
                    movieDto.getGenres().stream().map(GenreDto::getId).collect(Collectors.toList()));
            movie.setGenres(Genres);
        }

        Movie saved = movieRepository.save(movie);
        return movieMapper.toDto(saved);
    }

    /**
     * Cập nhật thông tin của một bộ phim hiện có.
     * 
     * @param id       ID của phim cần cập nhật.
     * @param movieDto Dữ liệu mới.
     * @return MovieDto sau khi cập nhật.
     */
    @Transactional
    public MovieDto updateMovie(Integer id, MovieDto movieDto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        movieMapper.updateEntity(movieDto, movie);
        if (movieDto.getIsFeatured() != null)
            movie.setIsFeatured(movieDto.getIsFeatured());

        if (movieDto.getGenres() != null) {
            List<Genre> Genres = GenreRepository.findAllById(
                    movieDto.getGenres().stream().map(GenreDto::getId).collect(Collectors.toList()));
            movie.setGenres(Genres);
        }

        Movie saved = movieRepository.save(movie);
        return movieMapper.toDto(saved);
    }

    /**
     * "Xóa" một bộ phim bằng cách chuyển trạng thái sang HIDDEN.
     * 
     * @param id ID của phim cần xóa.
     */
    @Transactional
    public void deleteMovie(Integer id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim"));
        if (showtimeRepository.existsByMovieIdAndStartTimeAfter(id, LocalDateTime.now())) {
            throw new RuntimeException("Không thể xóa phim vì đang có suất chiếu trong tương lai");
        }
        movie.setStatus(MovieStatus.HIDDEN);
        movieRepository.save(movie);
    }

    /**
     * Chuyển đổi Movie sang MovieDto và xử lý URL của poster.
     */
    private MovieDto toDtoWithPoster(Movie movie) {
        MovieDto dto = movieMapper.toDto(movie);
        dto.setPosterUrl(resolvePosterUrl(movie));
        return dto;
    }

    /**
     * Chuyển đổi Movie sang MovieItemDTO kèm theo URL poster và thông tin đánh giá.
     */
    private MovieItemDTO toItemDtoWithPoster(Movie movie) {
        MovieItemDTO dto = movieMapper.toItemDto(movie);
        dto.setPosterUrl(resolvePosterUrl(movie));
        enrichItemWithRating(dto, movie.getId());
        return dto;
    }

    /**
     * Xử lý và trả về URL tuyệt đối của poster phim (hỗ trợ cả link Cloudinary và
     * link nội bộ).
     */
    private String resolvePosterUrl(Movie movie) {
        if (movie.getPosterUrl() == null)
            return null;
        if (movie.getPosterUrl().startsWith("http"))
            return movie.getPosterUrl();

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/movies/")
                .path(movie.getId().toString())
                .path("/image")
                .toUriString();
    }

    /**
     * Bổ sung thông tin về điểm trung bình và số lượng đánh giá vào MovieItemDTO.
     */
    private void enrichItemWithRating(MovieItemDTO dto, Integer movieId) {
        Double avg = reviewRepository.getAverageRatingByMovieId(movieId);
        Long count = reviewRepository.getReviewCountByMovieId(movieId);
        dto.setAverageRating(avg != null ? avg : 0.0);
        dto.setReviewCount(count != null ? count.intValue() : 0);

        List<Object[]> distribution = reviewRepository.getRatingDistributionByMovieId(movieId);
        java.util.Map<Integer, Integer> distMap = new java.util.HashMap<>();
        for (Object[] obj : distribution) {
            distMap.put((Integer) obj[0], ((Long) obj[1]).intValue());
        }
        dto.setRatingDistribution(distMap);
    }

    /**
     * Bổ sung URL poster vào MovieDetailDTO.
     */
    private void enrichDetailWithPoster(MovieDetailDTO dto, Movie movie) {
        dto.setPosterUrl(resolvePosterUrl(movie));
    }

    /**
     * Bổ sung thông tin đánh giá chi tiết vào MovieDetailDTO.
     */
    private void enrichDetailWithRating(MovieDetailDTO dto, Integer movieId) {
        Double avg = reviewRepository.getAverageRatingByMovieId(movieId);
        Long count = reviewRepository.getReviewCountByMovieId(movieId);
        dto.setAverageRating(avg != null ? avg : 0.0);
        dto.setReviewCount(count != null ? count.intValue() : 0);

        List<Object[]> distribution = reviewRepository.getRatingDistributionByMovieId(movieId);
        java.util.Map<Integer, Integer> distMap = new java.util.HashMap<>();
        for (Object[] obj : distribution) {
            distMap.put((Integer) obj[0], ((Long) obj[1]).intValue());
        }
        dto.setRatingDistribution(distMap);
    }

    /**
     * Tải poster phim lên Cloudinary và lưu lại URL mới.
     * 
     * @param id   ID của phim.
     * @param file File ảnh cần tải lên.
     */
    @Transactional
    public void uploadMoviePoster(Integer id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        try {
            String url = cloudinaryService.uploadImage(file);
            movie.setPosterUrl(url);
            movieRepository.save(movie);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    /**
     * Phương thức dự phòng để lấy dữ liệu ảnh thô (hiện tại không còn dùng vì đã
     * chuyển sang Cloudinary).
     */
    public byte[] getMoviePoster(Integer id) {
        return new byte[0];
    }

}
