package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.MovieDto;
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

    @GetMapping
    public ResponseEntity<RestResponse<List<MovieDto>>> getAllMovies() {
        List<MovieDto> movies = movieService.getAllMovies();
        return ResponseEntity.ok(RestResponse.success(movies, "Fetched movies successfully"));
    }

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

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<MovieDto>> createMovie(@RequestBody MovieDto movieDto) {
        MovieDto createdMovie = movieService.createMovie(movieDto);
        return ResponseEntity.ok(RestResponse.success(createdMovie, "Created movie successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<MovieDto>> updateMovie(@PathVariable Integer id, @RequestBody MovieDto movieDto) {
        try {
            MovieDto updatedMovie = movieService.updateMovie(id, movieDto);
            return ResponseEntity.ok(RestResponse.success(updatedMovie, "Updated movie successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }

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

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<Void>> uploadMoviePoster(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            movieService.uploadMoviePoster(id, file);
            return ResponseEntity.ok(RestResponse.success(null, "Uploaded poster successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Upload Failed", e.getMessage()));
        }
    }

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
