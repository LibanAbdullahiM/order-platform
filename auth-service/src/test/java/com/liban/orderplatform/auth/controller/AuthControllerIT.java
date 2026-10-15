package com.liban.orderplatform.auth.controller;

import com.liban.orderplatform.auth.dto.AuthResponse;
import com.liban.orderplatform.auth.dto.LoginRequest;
import com.liban.orderplatform.auth.dto.RegisterRequest;
import com.liban.orderplatform.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
         "postgres:16-alpine")
            .withDatabaseName("auth-test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "liban4243@gmail.com",
                "liban@1234"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.role").value("USER"));

    }

    @Test
    void loginAndGetCurrentUser() throws Exception {
        LoginRequest request = new LoginRequest(
                "liban4243@gmail.com",
                "liban@1234"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").exists())
                .andExpect(jsonPath("$.expiresInSeconds").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + authResponse.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("liban4243@gmail.com"))
                .andExpect(jsonPath("$.role").exists());


    }

    @Test
    void shouldFailLogin() throws Exception {
        LoginRequest request = new LoginRequest(
                "liban4243@gmail.com",
                "liban@"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.instance").value("/api/auth/login"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));

    }
}