package com.example.LearningBackend.controller;

import com.example.LearningBackend.dto.UserResponse;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        User user = userService.getUserEntityByEmail(authentication.getName());
        return ResponseEntity.ok(userService.toUserResponse(user));
    }

    @PutMapping("/api/users/me")
    public ResponseEntity<UserResponse> updateMyProfile(Authentication authentication,
                                                        @RequestBody User updatedUser) {
        User user = userService.getUserEntityByEmail(authentication.getName());
        UserResponse response = userService.updateUser(user.getId(), updatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsersForAdmin() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserForAdmin(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully", "id", id.toString()));
    }
}