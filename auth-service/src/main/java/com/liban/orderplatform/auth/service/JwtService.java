package com.liban.orderplatform.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    @Getter
    private final long expiresInSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {

        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException(
                    "JWT_SECRET must be valid Base64 and decode to at least 32 bytes");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        }catch (RuntimeException e) {
            throw new IllegalStateException( "JWT_SECRET must be valid Base64 and decode to at least 32 bytes", e);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be valid Base64 and decode to at least 32 bytes");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiresInSeconds = Math.multiplyExact(expirationMinutes, 60);
    }

    public String generateToken(String subject, String role) {
        Instant now = Instant.now();
        Instant expireAt =  now.plusSeconds(expiresInSeconds);

        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(signingKey)
                .compact();
    }


    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String subject = extractSubject(token);
        return (subject.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(Instant.now()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
