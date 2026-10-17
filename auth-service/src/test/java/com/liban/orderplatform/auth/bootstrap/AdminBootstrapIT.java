package com.liban.orderplatform.auth.bootstrap;

import com.liban.orderplatform.auth.PostgresTestConfiguration;
import com.liban.orderplatform.auth.dto.AuthResponse;
import com.liban.orderplatform.auth.dto.LoginRequest;
import com.liban.orderplatform.auth.entity.User;
import com.liban.orderplatform.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
public class AdminBootstrapIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminBootstrap adminBootstrap;

    @Autowired
    MockMvc mockMvc;

    @Test
    void loginAndMe() throws Exception {
        LoginRequest request = new LoginRequest(
                "test-admin@email.com",
                "test-password123"
        );

        //Test /login
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        //Test /me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + authResponse.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldCreateAdminOnStartupAndNotCreateDuplicate() throws Exception {
        // Verify the admin was created on startup application
        String email = "test-admin@email.com";

        assertThat(userRepository.existsByEmail(email)).isTrue();
        User user = userRepository.findByEmail(email).orElse(null);

        assertThat(user.getRole().name()).isEqualTo("ADMIN");
        assertThat(user.isEnabled()).isEqualTo(true);


        //Run the bootstrap logic again manually
        adminBootstrap.run(new DefaultApplicationArguments());

        User user2 = userRepository.findByEmail(email).orElse(null);

        //verify that running it again did not create a duplicate
        assertThat(user.getId()).isEqualTo(user2.getId());
        assertThat(user.getPasswordHash()).isEqualTo(user2.getPasswordHash());
    }
}
