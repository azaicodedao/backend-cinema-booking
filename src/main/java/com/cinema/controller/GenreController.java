package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.GenreDto;
import com.cinema.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/Genres")
public class GenreController {

    @Autowired
    private GenreService GenreService;

    @GetMapping
    public ResponseEntity<RestResponse<List<GenreDto>>> getAllGenres() {
        List<GenreDto> Genres = GenreService.getAllGenres();
        return ResponseEntity.ok(RestResponse.success(Genres, "Fetched Genres successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<GenreDto>> createGenre(@RequestBody GenreDto GenreDto) {
        GenreDto saved = GenreService.createGenre(GenreDto);
        return ResponseEntity.ok(RestResponse.success(saved, "Created Genre successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<GenreDto>> updateGenre(@PathVariable Integer id, @RequestBody GenreDto GenreDto) {
        try {
            GenreDto updated = GenreService.updateGenre(id, GenreDto);
            return ResponseEntity.ok(RestResponse.success(updated, "Updated Genre successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }
}
