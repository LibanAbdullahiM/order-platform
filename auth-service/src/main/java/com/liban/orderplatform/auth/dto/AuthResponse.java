package com.liban.orderplatform.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds
) {
}
