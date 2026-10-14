package com.example.LearningBackend.service;

import com.example.LearningBackend.dto.AuthRequest;
import com.example.LearningBackend.dto.AuthResponse;
import com.example.LearningBackend.dto.RegisterRequest;
import com.example.LearningBackend.dto.UserResponse;
import com.example.LearningBackend.exception.BadRequestException;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.repository.UserRepository;
import com.example.LearningBackend.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        String role = request.getRole();
        if (role == null || role.isBlank()) {
            role = "student";
        } else {
            role = role.trim().toLowerCase();
        }

        // Security rule: Public registration must never create an ADMIN account
        if ("admin".equals(role) || "role_admin".equals(role)) {
            throw new BadRequestException("Public registration as administrator is strictly forbidden");
        }

        if (!"student".equals(role) && !"instructor".equals(role)) {
            role = "student";
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser);
        UserResponse userResponse = userService.toUserResponse(savedUser);

        return new AuthResponse(token, userResponse);
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user);
        UserResponse userResponse = userService.toUserResponse(user);

        return new AuthResponse(token, userResponse);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new BadRequestException("User not found: " + email));
        return userService.toUserResponse(user);
    }
}
