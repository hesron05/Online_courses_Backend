package com.example.LearningBackend.service;

import com.example.LearningBackend.exception.BadRequestException;
import com.example.LearningBackend.exception.ResourceNotFoundException;
import com.example.LearningBackend.model.Course;
import com.example.LearningBackend.model.Enrollment;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.repository.CourseRepository;
import com.example.LearningBackend.repository.EnrollmentRepository;
import com.example.LearningBackend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public Enrollment enrollStudent(Long courseId, String studentEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BadRequestException("You are already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment(student.getId(), courseId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    public List<Map<String, Object>> getStudentEnrolledCourses(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Optional<Course> courseOpt = courseRepository.findById(enrollment.getCourseId());
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                Map<String, Object> item = new HashMap<>();
                item.put("enrollmentId", enrollment.getId());
                item.put("courseId", course.getId());
                item.put("title", course.getTitle());
                item.put("description", course.getDescription());
                item.put("instructor", course.getInstructor());
                item.put("category", course.getCategory());
                item.put("difficulty", course.getDifficulty());
                item.put("totalLessons", course.getLessons());
                item.put("progress", enrollment.getProgressPercentage());
                item.put("completed", enrollment.getCompleted());
                item.put("enrolledAt", enrollment.getEnrolledAt());
                result.add(item);
            }
        }
        return result;
    }

    public boolean isStudentEnrolled(Long courseId, String studentEmail) {
        Optional<User> studentOpt = userRepository.findByEmail(studentEmail);
        if (studentOpt.isEmpty()) return false;
        return enrollmentRepository.existsByStudentIdAndCourseId(studentOpt.get().getId(), courseId);
    }

    public List<Map<String, Object>> getCourseStudents(Long courseId, String userEmail, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isAdmin && (course.getInstructorId() == null || !course.getInstructorId().equals(user.getId()))) {
            throw new AccessDeniedException("You are not authorized to view enrolled students for this course");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> students = new ArrayList<>();

        for (Enrollment e : enrollments) {
            Optional<User> studentOpt = userRepository.findById(e.getStudentId());
            if (studentOpt.isPresent()) {
                User s = studentOpt.get();
                Map<String, Object> map = new HashMap<>();
                map.put("studentId", s.getId());
                map.put("studentName", s.getName());
                map.put("studentEmail", s.getEmail());
                map.put("progress", e.getProgressPercentage());
                map.put("completed", e.getCompleted());
                map.put("enrolledAt", e.getEnrolledAt());
                students.add(map);
            }
        }
        return students;
    }
}
