package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.user.*;
import com.ecommerce.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================
    // Authentication APIs
    // =========================

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return userService.register(request);
    }

    @PostMapping("/auth/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {

        return userService.login(request);
    }

    // =========================
    // Admin User Management
    // =========================

    @GetMapping("/users/admin")
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    @PutMapping("/users/admin/{userId}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {

        return userService.updateUserStatus(
                userId,
                request
        );
    }


    @PutMapping("/users/admin/{userId}/role")
    public UserResponse updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return userService.updateUserRole(
                userId,
                request
        );
    }


}

