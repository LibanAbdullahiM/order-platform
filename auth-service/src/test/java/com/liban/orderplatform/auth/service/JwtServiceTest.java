package com.liban.orderplatform.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;


import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // 32 bytes, Base64-encoded, suitable for an HMAC-SHA256 key
    private static final String TEST_JWT_SECRET = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef"
                    .getBytes(StandardCharsets.UTF_8)
    );

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_JWT_SECRET, 60);
    }

    @Test
    void validToken() {
        String subject = "libanr4243@gmail.com";
        String role = "USER";

        String token = jwtService.generateToken(subject, role);

        UserDetails user = User.withUsername(subject)
                .password("password")
                .roles(role)
                .build();

        //Act
        boolean valid = jwtService.validateToken(token, user);

        // Assert
        assertTrue(valid);
        assertNotNull(token);
        assertEquals(subject, user.getUsername());
    }

    @Test
    void expiredToken() {
        // Arrange: create a correct signed token that has already expired
        Instant now = Instant.now();

        String expiredToke = Jwts.builder()
                .subject("libanr4243@gmail.com")
                .claim("role", "USER")
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(
                        Base64.getDecoder().decode(TEST_JWT_SECRET)
                ))
                .compact();

        //Act + Assert
        assertThrows(
                JwtException.class,
                () -> jwtService.validateToken(expiredToke, null));
    }

    @Test
    void tamperedToken() {
        String subject = "libanr4243@gmail.com";
        String role = "USER";

        String token = jwtService.generateToken(subject, role);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // change the signature segment
        String signature = parts[2];
        char replacement = signature.charAt(0) == 'a' ? 'b' : 'a';
        parts[2] = replacement + signature.substring(1);

        String tamperedToken = String.join(".", parts);

        // Act + assert
        assertThrows(
                JwtException.class,
                () -> jwtService.validateToken(tamperedToken, null)
        );
    }
}