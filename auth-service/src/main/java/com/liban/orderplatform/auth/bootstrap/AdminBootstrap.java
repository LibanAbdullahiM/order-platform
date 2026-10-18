package com.liban.orderplatform.auth.bootstrap;

import com.liban.orderplatform.auth.entity.Role;
import com.liban.orderplatform.auth.entity.User;
import com.liban.orderplatform.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Slf4j
@Component
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String email = adminProperties.email().trim().toLowerCase();
        String password = adminProperties.password();

        boolean hasEmail = StringUtils.hasText(email);
        boolean hasPassword = StringUtils.hasText(password);

        if (!hasEmail && !hasPassword) {
            log.info("AdminBootstrap is skipped: No admin credentials configured.");
            return;
        }

        if (hasEmail ^ hasPassword) {
            String missingProp = !hasEmail ? "ADMIN_EMAIL" : "ADMIN_PASSWORD";
            throw new IllegalStateException("Failed to initialize AdminBootstrap: " +
                    "Half-configured admin credentials. " +
                    "Missing required property: " +  missingProp);

        }

        if (!isValidPassword(password)) {
            throw new IllegalStateException("Failed to initialize AdminBootstrap: " +
                    "Provided admin password does not satisfy security policy requirements. " +
                    "ADMIN_PASSWORD must be 8 to 72 characters");
        }

        User existing = userRepository.findByEmail(email).orElse(null);

        if (existing != null) {
            String role = existing.getRole().name().equals("USER") ? "USER" : "ADMIN";
            String message =
                            "User with email " + email + " already exists as " + role + ". Skipping admin creation.";
            if (role.equals("ADMIN")) {
                log.info(message);
            }else {
                log.warn(message);
            }
            return;
        }

        registerAdmin(email, password);

    }

    private void registerAdmin(String email, String password) {

        try {
            String passwordHash = passwordEncoder.encode(password);

            User user = new User();

            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setRole(Role.ADMIN);
            user.setEnabled(true);
            user.setCreatedAt(Instant.now());

            User savedUser = userRepository.save(user);

            log.info("Admin is created with email: {}", savedUser.getEmail());

        }catch(DataIntegrityViolationException ex) {
            log.info("Admin already exists with email: {}", email);
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 72;
    }
}
