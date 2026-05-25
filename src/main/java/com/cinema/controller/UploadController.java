package com.cinema.controller;

import com.cinema.service.Cloudinary.CloudinaryService;
import com.cinema.dto.response.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<Map<String, String>>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(RestResponse.error(400, "Bad Request", "File is empty"));
            }
            String url = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(RestResponse.success(Map.of("url", url), "Upload successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(RestResponse.error(400, "Upload Failed", e.getMessage()));
        }
    }
}
