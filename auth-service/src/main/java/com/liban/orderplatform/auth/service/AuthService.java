package com.liban.orderplatform.auth.service;

import com.liban.orderplatform.auth.dto.AuthResponse;
import com.liban.orderplatform.auth.dto.LoginRequest;
import com.liban.orderplatform.auth.dto.RegisterRequest;
import com.liban.orderplatform.auth.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser();
}
