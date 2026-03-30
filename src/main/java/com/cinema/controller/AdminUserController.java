package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.enums.Role;
import com.cinema.entity.User;
import com.cinema.enums.UserStatus;
import com.cinema.security.services.UserDetailsImpl;
import com.cinema.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    private Integer getCurrentAdminId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    @GetMapping
    public ResponseEntity<RestResponse<Page<User>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = adminUserService.getUsers(keyword, role, status, pageable);
        return ResponseEntity.ok(RestResponse.success(users, "Users fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<User>> getUserDetail(@PathVariable Integer id) {
        try {
            User user = adminUserService.getUserDetail(id);
            return ResponseEntity.ok(RestResponse.success(user, "User fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<RestResponse<Void>> changeRole(@PathVariable Integer id, @RequestParam Role role) {
        try {
            adminUserService.changeRole(getCurrentAdminId(), id, role);
            return ResponseEntity.ok(RestResponse.<Void>success(null, "Role changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<RestResponse<Void>> lockUser(@PathVariable Integer id) {
        try {
            adminUserService.lockUser(getCurrentAdminId(), id);
            return ResponseEntity.ok(RestResponse.<Void>success(null, "User locked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<RestResponse<Void>> unlockUser(@PathVariable Integer id) {
        try {
            adminUserService.unlockUser(getCurrentAdminId(), id);
            return ResponseEntity.ok(RestResponse.<Void>success(null, "User unlocked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }
}
