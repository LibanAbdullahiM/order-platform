package com.liban.orderplatform.auth.bootstrap;

import com.liban.orderplatform.auth.entity.Role;
import com.liban.orderplatform.auth.entity.User;
import com.liban.orderplatform.auth.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private final Environment environment;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(Environment environment,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String email = environment.getProperty("admin.email");
        String password = environment.getProperty("admin.password");

        boolean adminExists = userRepository.existsByEmail(email);

        if (!adminExists && email != null && password != null) {
            String passwordHash = passwordEncoder.encode(password);

            User user = new User();

            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setRole(Role.ADMIN);
            user.setEnabled(true);
            user.setCreatedAt(Instant.now());

            userRepository.save(user);
        }

    }
}
