package com.cinema.controller;

import com.cinema.dto.MovieDetailDTO;
import com.cinema.dto.MovieDto;
import com.cinema.dto.MovieItemDTO;
import com.cinema.dto.response.RestResponse;
import com.cinema.enums.MovieStatus;
import com.cinema.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    /**
     * Lấy danh sách tất cả các phim.
     * 
     * @return Danh sách MovieDto.
     */
    @GetMapping
    public ResponseEntity<RestResponse<List<MovieDto>>> getAllMovies() {
        List<MovieDto> movies = movieService.getAllMovies();
        return ResponseEntity.ok(RestResponse.success(movies, "Fetched all movies successfully"));
    }

    /**
     * Lấy danh sách phim nổi bật (để hiển thị trên Carousel).
     * 
     * @return Danh sách MovieItemDTO.
     */
    @GetMapping("/featured")
    public ResponseEntity<RestResponse<List<MovieItemDTO>>> getFeaturedMovies() {
        List<MovieItemDTO> movies = movieService.getFeaturedMovies();
        return ResponseEntity.ok(RestResponse.success(movies, "Fetched featured movies successfully"));
    }

    /**
     * Lấy danh sách phim đang chiếu.
     * 
     * @return Danh sách MovieItemDTO.
     */
    @GetMapping("/showing")
    public ResponseEntity<RestResponse<List<MovieItemDTO>>> getShowingMovies() {
        List<MovieItemDTO> movies = movieService.getShowingMovies();
        return ResponseEntity.ok(RestResponse.success(movies, "Fetched now showing movies successfully"));
    }

    /**
     * Lấy danh sách phim sắp chiếu.
     * 
     * @return Danh sách MovieItemDTO.
     */
    @GetMapping("/coming-soon")
    public ResponseEntity<RestResponse<List<MovieItemDTO>>> getComingSoonMovies() {
        List<MovieItemDTO> movies = movieService.getComingSoonMovies();
        return ResponseEntity.ok(RestResponse.success(movies, "Fetched coming soon movies successfully"));
    }

    /**
     * Tìm kiếm và lọc phim theo tên, thể loại và trạng thái.
     * 
     * @param title   Tiêu đề phim (tùy chọn).
     * @param genreId ID thể loại (tùy chọn).
     * @param status  Trạng thái phim (tùy chọn).
     * @return Danh sách MovieItemDTO phù hợp tiêu chí.
     */
    @GetMapping("/search")
    public ResponseEntity<RestResponse<List<MovieItemDTO>>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) MovieStatus status) {
        List<MovieItemDTO> movies = movieService.searchMovies(title, genreId, status);
        return ResponseEntity.ok(RestResponse.success(movies, "Search results fetched successfully"));
    }

    /**
     * Lấy thông tin cơ bản của một bộ phim theo ID.
     * 
     * @param id ID của phim.
     * @return MovieDto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<MovieDto>> getMovieById(@PathVariable Integer id) {
        try {
            MovieDto movie = movieService.getMovieById(id);
            return ResponseEntity.ok(RestResponse.success(movie, "Fetched movie successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * Lấy thông tin chi tiết của một bộ phim (bao gồm đánh giá) theo ID.
     * 
     * @param id ID của phim.
     * @return MovieDetailDTO.
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<RestResponse<MovieDetailDTO>> getMovieDetail(@PathVariable Integer id) {
        try {
            MovieDetailDTO movie = movieService.getMovieDetail(id);
            return ResponseEntity.ok(RestResponse.success(movie, "Fetched movie detail successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * Tạo phim mới (Yêu cầu quyền ADMIN).
     * 
     * @param movieDto Dữ liệu phim.
     * @return MovieDto đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<MovieDto>> createMovie(@RequestBody MovieDto movieDto) {
        MovieDto createdMovie = movieService.createMovie(movieDto);
        return ResponseEntity.ok(RestResponse.success(createdMovie, "Created movie successfully"));
    }

    /**
     * Cập nhật thông tin phim (Yêu cầu quyền ADMIN).
     * 
     * @param id       ID của phim cần cập nhật.
     * @param movieDto Dữ liệu mới.
     * @return MovieDto sau khi cập nhật.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<MovieDto>> updateMovie(@PathVariable Integer id,
            @RequestBody MovieDto movieDto) {
        try {
            MovieDto updatedMovie = movieService.updateMovie(id, movieDto);
            return ResponseEntity.ok(RestResponse.success(updatedMovie, "Updated movie successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * Xóa phim (chuyển sang trạng thái ENDED - Yêu cầu quyền ADMIN).
     * 
     * @param id ID của phim.
     * @return Không có nội dung trả về.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<Void>> deleteMovie(@PathVariable Integer id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.ok(RestResponse.success(null, "Deleted movie successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * Tải ảnh poster phim lên Cloudinary (Yêu cầu quyền ADMIN).
     * 
     * @param id   ID của phim.
     * @param file File ảnh.
     * @return Không có nội dung trả về.
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<Void>> uploadMoviePoster(@PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            movieService.uploadMoviePoster(id, file);
            return ResponseEntity.ok(RestResponse.success(null, "Uploaded poster successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Upload Failed", e.getMessage()));
        }
    }

    /**
     * Phương thức dự phòng để lấy ảnh (hiện không dùng vì dùng link Cloudinary trực
     * tiếp).
     * 
     * @param id ID của phim.
     * @return Dữ liệu byte của ảnh.
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getMoviePoster(@PathVariable Integer id) {
        try {
            byte[] imageBytes = movieService.getMoviePoster(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
