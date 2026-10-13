package com.liban.orderplatform.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
