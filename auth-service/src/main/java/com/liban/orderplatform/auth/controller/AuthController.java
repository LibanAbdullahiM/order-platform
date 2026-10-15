package com.liban.orderplatform.auth.controller;

import com.liban.orderplatform.auth.dto.AuthResponse;
import com.liban.orderplatform.auth.dto.LoginRequest;
import com.liban.orderplatform.auth.dto.RegisterRequest;
import com.liban.orderplatform.auth.dto.UserResponse;
import com.liban.orderplatform.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return authService.getCurrentUser();
    }
}
