package com.liban.orderplatform.auth.service;

import com.liban.orderplatform.auth.dto.AuthResponse;
import com.liban.orderplatform.auth.dto.LoginRequest;
import com.liban.orderplatform.auth.dto.RegisterRequest;
import com.liban.orderplatform.auth.dto.UserResponse;
import com.liban.orderplatform.auth.entity.Role;
import com.liban.orderplatform.auth.entity.User;
import com.liban.orderplatform.auth.exception.EmailAlreadyExistException;
import com.liban.orderplatform.auth.mapper.UserMapper;
import com.liban.orderplatform.auth.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper mapper,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistException("Email already exists!");
        }

        //Encode password
        String password_hash = passwordEncoder.encode(request.password());

        User user = new  User();

        user.setEmail(email);
        user.setPasswordHash(password_hash);
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());

        User savedUser = null;


        try {
            //save the User entity
            savedUser = userRepository.save(user);

        }catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistException("Email already exists!");
        }

        return mapper.toUserResponse(savedUser);

    }

    @Override
    public AuthResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

         UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String subject = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        String token = jwtService.generateToken(subject, role);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpiresInSeconds()
        );
    }

    @Override
    public UserResponse getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        assert auth != null;
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        assert userDetails != null;
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));

        return mapper.toUserResponse(user);
    }
}
