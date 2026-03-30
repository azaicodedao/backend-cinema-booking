package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.request.LoginRequest;
import com.cinema.dto.request.SignupRequest;
import com.cinema.dto.request.TokenRefreshRequest;
import com.cinema.dto.response.TokenResponse;
import com.cinema.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<RestResponse<TokenResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            TokenResponse tokenResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok().body(RestResponse.success(tokenResponse, "Login successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<RestResponse<Void>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            String message = authService.registerUser(signUpRequest);
            return ResponseEntity.ok().body(RestResponse.<Void>success(null, message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "Error during registration"));
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<RestResponse<TokenResponse>> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            TokenResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(RestResponse.success(response, "Token refreshed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(RestResponse.error(403, "Forbidden", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logoutUser() {
        try {
            // Usually we'd get the current user ID from the context
            // authService.logout(getCurrentUserId());
            return ResponseEntity.ok(RestResponse.<Void>success(null, "Log out successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Error", "Could not log out"));
        }
    }
}
