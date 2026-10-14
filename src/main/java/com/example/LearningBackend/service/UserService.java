package com.example.LearningBackend.service;

import com.example.LearningBackend.dto.DashboardStatsResponse;
import com.example.LearningBackend.dto.UserResponse;
import com.example.LearningBackend.exception.ResourceNotFoundException;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.repository.CourseRepository;
import com.example.LearningBackend.repository.EnrollmentRepository;
import com.example.LearningBackend.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CourseRepository courseRepository,
                       EnrollmentRepository enrollmentRepository,
                       @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toUserResponse(user);
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User createUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        return userRepository.save(user);
    }

    public UserResponse updateUser(Long id, User newUser) {
        User user = getUserEntityById(id);
        if (newUser.getName() != null) user.setName(newUser.getName());
        if (newUser.getEmail() != null) user.setEmail(newUser.getEmail());
        if (newUser.getPassword() != null && !newUser.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }
        if (newUser.getRole() != null) user.setRole(newUser.getRole());
        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRole("student") + userRepository.countByRole("ROLE_STUDENT");
        long totalInstructors = userRepository.countByRole("instructor") + userRepository.countByRole("ROLE_INSTRUCTOR");
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();

        return new DashboardStatsResponse(totalUsers, totalStudents, totalInstructors, totalCourses, totalEnrollments);
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}