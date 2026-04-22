package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.entity.User;
import com.cinema.dto.request.ChangePasswordRequest;
import com.cinema.dto.request.UpdateProfileRequest;
import com.cinema.security.services.UserDetailsImpl;
import com.cinema.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private Integer getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    @GetMapping({"/profile", "/me/profile"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestResponse<User>> getProfile() {
        try {
            User user = userService.getProfile(getCurrentUserId());
            return ResponseEntity.ok(RestResponse.success(user, "Profile fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    @PutMapping({"/profile", "/me/profile"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestResponse<User>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            User updatedUser = userService.updateProfile(getCurrentUserId(), request);
            return ResponseEntity.ok(RestResponse.success(updatedUser, "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    @PutMapping({"/password", "/me/password"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(getCurrentUserId(), request);
            return ResponseEntity.ok(RestResponse.<Void>success(null, "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Error", "Could not change password"));
        }
    }
}
