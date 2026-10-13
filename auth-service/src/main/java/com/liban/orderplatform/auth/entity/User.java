package com.liban.orderplatform.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "created_at")
    private Instant createdAt;

    public User(){}

    public User(String email,
                String password_hash,
                String role,
                Boolean enabled,
                Instant createdAt) {
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;

    }
}
