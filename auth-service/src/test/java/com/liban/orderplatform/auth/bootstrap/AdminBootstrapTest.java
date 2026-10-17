package com.liban.orderplatform.auth.bootstrap;

import com.liban.orderplatform.auth.entity.Role;
import com.liban.orderplatform.auth.entity.User;
import com.liban.orderplatform.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminBootstrapTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminProperties adminProperties;

    private AdminBootstrap adminBootstrap;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);

    }

    @Test
    void shouldSkipUnsetProperties() throws Exception {
        adminProperties = new AdminProperties("", "");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        assertDoesNotThrow(() -> adminBootstrap.run(new DefaultApplicationArguments()));
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenHalfConfigured() throws Exception {
        adminProperties = new  AdminProperties("test@gmail.com", "");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        assertThrows(IllegalStateException.class, () -> adminBootstrap.run(new DefaultApplicationArguments()));
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWithInvalidPassword() throws Exception {
        adminProperties = new  AdminProperties("test@gmail.com", "test");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        assertThrows(IllegalStateException.class, () -> adminBootstrap.run(new DefaultApplicationArguments()));
        verifyNoInteractions(userRepository);
    }

    @Test
    void emailExistsWithRoleUser() throws Exception {
        adminProperties = new AdminProperties("test@gmail.com", "test@1234");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> adminBootstrap.run(new DefaultApplicationArguments()));
        verify(userRepository, times(1)).existsByEmail("test@gmail.com");
        verify(userRepository, times(1)).findByEmail("test@gmail.com");

    }

    @Test
    void shouldSaveAdmin() throws Exception {
        adminProperties = new AdminProperties("Admin@x.com", "test@1234");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenReturn(new User());

        //Run the bootstrap
        adminBootstrap.run(new DefaultApplicationArguments());

        User user = captor.getValue();
        verify(userRepository, times(1)).save(user);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getEmail()).isEqualTo(adminProperties.email().trim().toLowerCase());
        assertNotEquals(user.getPasswordHash(), adminProperties.password());

    }
}
