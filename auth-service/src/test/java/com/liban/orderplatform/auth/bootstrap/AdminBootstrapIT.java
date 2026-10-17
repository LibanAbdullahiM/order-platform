package com.liban.orderplatform.auth.bootstrap;

import com.liban.orderplatform.auth.PostgresTestConfiguration;
import com.liban.orderplatform.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
public class AdminBootstrapIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminBootstrap adminBootstrap;

    @Test
    void shouldCreateAdminOnStartupAndNotCreateDuplicate() throws Exception {
        // Verify the admin was created on startup application
        assertThat(userRepository.existsByEmail("test-admin@email.com")).isTrue();
        long initialCount = userRepository.count();
        assertThat(initialCount).isEqualTo(1);

        //Run the bootstrap logic again manually
        adminBootstrap.run(null);

        //verify that running it again did not create a duplicate
        long finalCount = userRepository.count();
        assertThat(finalCount).isEqualTo(1);
    }
}
