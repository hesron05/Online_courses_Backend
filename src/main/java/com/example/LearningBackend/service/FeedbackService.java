package com.example.LearningBackend.service;

import com.example.LearningBackend.dto.FeedbackRequest;
import com.example.LearningBackend.exception.BadRequestException;
import com.example.LearningBackend.exception.ResourceNotFoundException;
import com.example.LearningBackend.model.Course;
import com.example.LearningBackend.model.Feedback;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.repository.CourseRepository;
import com.example.LearningBackend.repository.EnrollmentRepository;
import com.example.LearningBackend.repository.FeedbackRepository;
import com.example.LearningBackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           CourseRepository courseRepository,
                           UserRepository userRepository,
                           EnrollmentRepository enrollmentRepository) {
        this.feedbackRepository = feedbackRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public Feedback submitFeedback(Long courseId, FeedbackRequest request, String studentEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BadRequestException("Only enrolled students can submit feedback for this course");
        }

        Optional<Feedback> existingOpt = feedbackRepository.findByStudentIdAndCourseId(student.getId(), courseId);
        Feedback feedback;
        if (existingOpt.isPresent()) {
            feedback = existingOpt.get();
            feedback.setRating(request.getRating());
            feedback.setComment(request.getComment());
            feedback.setCreatedAt(LocalDateTime.now());
        } else {
            feedback = new Feedback(
                    student.getId(),
                    student.getName(),
                    courseId,
                    request.getRating(),
                    request.getComment()
            );
        }

        Feedback saved = feedbackRepository.save(feedback);

        // Recalculate course average rating
        List<Feedback> allFeedbacks = feedbackRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        if (!allFeedbacks.isEmpty()) {
            double avg = allFeedbacks.stream()
                    .mapToInt(Feedback::getRating)
                    .average()
                    .orElse(5.0);
            double rounded = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
            course.setRating(rounded);
            courseRepository.save(course);
        }

        return saved;
    }

    public List<Feedback> getCourseFeedback(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return feedbackRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }
}
