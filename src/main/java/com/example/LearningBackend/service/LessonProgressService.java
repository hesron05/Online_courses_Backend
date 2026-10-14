package com.example.LearningBackend.service;

import com.example.LearningBackend.exception.BadRequestException;
import com.example.LearningBackend.exception.ResourceNotFoundException;
import com.example.LearningBackend.model.Enrollment;
import com.example.LearningBackend.model.Lesson;
import com.example.LearningBackend.model.LessonProgress;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.repository.EnrollmentRepository;
import com.example.LearningBackend.repository.LessonProgressRepository;
import com.example.LearningBackend.repository.LessonRepository;
import com.example.LearningBackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public LessonProgressService(LessonProgressRepository lessonProgressRepository,
                                 LessonRepository lessonRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 UserRepository userRepository) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> toggleLessonCompletion(Long lessonId, String studentEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Long courseId = lesson.getCourseId();
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new BadRequestException("You must be enrolled in this course to complete lessons"));

        Optional<LessonProgress> progressOpt = lessonProgressRepository.findByStudentIdAndLessonId(student.getId(), lessonId);
        LessonProgress progress;
        boolean newCompletedState;

        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            newCompletedState = !Boolean.TRUE.equals(progress.getCompleted());
            progress.setCompleted(newCompletedState);
            progress.setCompletedAt(newCompletedState ? LocalDateTime.now() : null);
        } else {
            newCompletedState = true;
            progress = new LessonProgress(student.getId(), lessonId, courseId, true);
        }
        lessonProgressRepository.save(progress);

        long totalLessons = lessonRepository.countByCourseId(courseId);
        long completedLessons = lessonProgressRepository.countByStudentIdAndCourseIdAndCompletedTrue(student.getId(), courseId);

        int progressPercentage = totalLessons > 0 ? (int) ((completedLessons * 100) / totalLessons) : 0;
        if (progressPercentage > 100) progressPercentage = 100;

        enrollment.setProgressPercentage(progressPercentage);
        enrollment.setCompleted(progressPercentage == 100);
        if (progressPercentage == 100 && enrollment.getCompletedAt() == null) {
            enrollment.setCompletedAt(LocalDateTime.now());
        }
        enrollmentRepository.save(enrollment);

        Map<String, Object> response = new HashMap<>();
        response.put("lessonId", lessonId);
        response.put("completed", newCompletedState);
        response.put("progressPercentage", progressPercentage);
        response.put("totalLessons", totalLessons);
        response.put("completedLessons", completedLessons);
        return response;
    }

    public Map<String, Object> getCourseProgress(Long courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId);
        int progressPercentage = enrollmentOpt.map(Enrollment::getProgressPercentage).orElse(0);
        boolean isEnrolled = enrollmentOpt.isPresent();

        List<LessonProgress> progressList = lessonProgressRepository.findByStudentIdAndCourseId(student.getId(), courseId);
        Set<Long> completedLessonIds = progressList.stream()
                .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                .map(LessonProgress::getLessonId)
                .collect(Collectors.toSet());

        Map<String, Object> result = new HashMap<>();
        result.put("courseId", courseId);
        result.put("isEnrolled", isEnrolled);
        result.put("progressPercentage", progressPercentage);
        result.put("completedLessonIds", completedLessonIds);
        return result;
    }
}
