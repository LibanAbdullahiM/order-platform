package com.liban.orderplatform.auth.bootstrap;

import com.liban.orderplatform.auth.entity.Role;
import com.liban.orderplatform.auth.entity.User;
import com.liban.orderplatform.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminProperties adminProperties;

    private AdminBootstrap adminBootstrap;

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

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> adminBootstrap.run(new DefaultApplicationArguments()));
        verify(userRepository, times(1)).findByEmail("test@gmail.com");
        verify(userRepository, never()).save(any());

    }

    @Test
    void shouldSaveAdmin() throws Exception {
        adminProperties = new AdminProperties("Admin@Example.com", "Strong-Password@1234");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(passwordEncoder.encode("Strong-Password@1234"))
                .thenReturn("$2a$12$CuDthhVk2pSZ6QVxFO0PzuEjNFSaIWGxi4e3VGW83RPvu2q5iB5JC");
        when(userRepository.save(captor.capture())).thenReturn(new User());

        //Run the bootstrap
        adminBootstrap.run(new DefaultApplicationArguments());

        User user = captor.getValue();
        verify(userRepository, times(1)).save(user);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getEmail()).isEqualTo("admin@example.com");
        assertNotEquals(user.getPasswordHash(), adminProperties.password());
        verify(passwordEncoder).encode("Strong-Password@1234");

    }

    @Test
    void adminExists() throws Exception {
        adminProperties = new AdminProperties("test@gmail.com", "Strong-Password@1234");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> adminBootstrap.run(new DefaultApplicationArguments()));
        verify(userRepository, times(1)).findByEmail("test@gmail.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowViolationException() throws Exception {
        adminProperties = new AdminProperties("test@example.com", "Strong-Password@123");
        adminBootstrap = new AdminBootstrap(userRepository, passwordEncoder, adminProperties);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Admin already exists"));

        assertDoesNotThrow(
                () -> adminBootstrap.run(new DefaultApplicationArguments()));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any());

    }
}
