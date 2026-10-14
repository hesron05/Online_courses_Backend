package com.example.LearningBackend.controller;

import com.example.LearningBackend.model.Enrollment;
import com.example.LearningBackend.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/api/enrollments/{courseId}")
    public ResponseEntity<Enrollment> enrollInCourse(@PathVariable Long courseId,
                                                    Authentication authentication) {
        Enrollment enrollment = enrollmentService.enrollStudent(courseId, authentication.getName());
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @GetMapping("/api/enrollments/my-courses")
    public ResponseEntity<List<Map<String, Object>>> getMyEnrolledCourses(Authentication authentication) {
        List<Map<String, Object>> list = enrollmentService.getStudentEnrolledCourses(authentication.getName());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/api/courses/{courseId}/students")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCourseStudents(@PathVariable Long courseId,
                                                                      Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Map<String, Object>> students = enrollmentService.getCourseStudents(courseId, authentication.getName(), isAdmin);
        return ResponseEntity.ok(students);
    }
}
