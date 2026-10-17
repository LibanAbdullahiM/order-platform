package com.liban.orderplatform.auth.controller;

import com.liban.orderplatform.auth.PostgresTestConfiguration;
import com.liban.orderplatform.auth.dto.AuthResponse;
import com.liban.orderplatform.auth.dto.LoginRequest;
import com.liban.orderplatform.auth.dto.RegisterRequest;
import com.liban.orderplatform.auth.repository.UserRepository;
import com.liban.orderplatform.auth.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
class AuthControllerIT {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    UserRepository repository;

    @BeforeEach
    void beforeEach() {

        repository.deleteAll();

        RegisterRequest request = new RegisterRequest(
                "test@gmail.com",
                "test@1234"
        );

        authService.register(request);

    }

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
    void registerWithInvalidRequestBody() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",
                ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.instance").value("/api/auth/register"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());

    }


    @Test
    void registerWithDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@gmail.com",
                "liban@1234"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.instance").value("/api/auth/register"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").exists());

    }

    @Test
    void loginAndGetCurrentUser() throws Exception {
        LoginRequest request = new LoginRequest(
                "test@gmail.com",
                "test@1234"
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
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.role").exists());


    }

    @Test
    void requestWithoutToken() throws Exception {

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.instance").value("/api/auth/me"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void requestWithGarbageBearerToken() throws Exception {

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer 0123456789abcdef0123456789abcdef"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.instance").value("/api/auth/me"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid or expired token"));

    }

    @Test
    void shouldSucceedLoginWithGarbageToken() throws Exception {
        LoginRequest request = new LoginRequest(
                "test@gmail.com",
                "test@1234"
        );

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer 0123456789abcdef0123456789abcdef")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").exists());
    }

    @Test
    void shouldFailLogin() throws Exception {
        LoginRequest request = new LoginRequest(
                "test@gmail.com",
                "test@"
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