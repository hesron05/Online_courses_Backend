package com.example.LearningBackend.service;

import com.example.LearningBackend.exception.ResourceNotFoundException;
import com.example.LearningBackend.model.Course;
import com.example.LearningBackend.model.Lesson;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.repository.CourseRepository;
import com.example.LearningBackend.repository.LessonRepository;
import com.example.LearningBackend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public LessonService(LessonRepository lessonRepository,
                         CourseRepository courseRepository,
                         UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<Lesson> getLessonsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId);
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
    }

    @Transactional
    public Lesson addLesson(Long courseId, Lesson lesson, String userEmail, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isAdmin && (course.getInstructorId() == null || !course.getInstructorId().equals(user.getId()))) {
            throw new AccessDeniedException("You are not authorized to add lessons to this course");
        }

        lesson.setCourseId(courseId);
        if (lesson.getLessonOrder() == null) {
            long count = lessonRepository.countByCourseId(courseId);
            lesson.setLessonOrder((int) count + 1);
        }
        if (lesson.getCreatedAt() == null) {
            lesson.setCreatedAt(LocalDateTime.now());
        }

        Lesson saved = lessonRepository.save(lesson);

        // Update total lessons count in Course
        long totalLessons = lessonRepository.countByCourseId(courseId);
        course.setLessons((int) totalLessons);
        courseRepository.save(course);

        return saved;
    }

    public Lesson updateLesson(Long id, Lesson newLesson, String userEmail, boolean isAdmin) {
        Lesson lesson = getLessonById(id);
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isAdmin && (course.getInstructorId() == null || !course.getInstructorId().equals(user.getId()))) {
            throw new AccessDeniedException("You are not authorized to modify lessons for this course");
        }

        if (newLesson.getTitle() != null) lesson.setTitle(newLesson.getTitle());
        if (newLesson.getDescription() != null) lesson.setDescription(newLesson.getDescription());
        if (newLesson.getContentUrl() != null) lesson.setContentUrl(newLesson.getContentUrl());
        if (newLesson.getLessonOrder() != null) lesson.setLessonOrder(newLesson.getLessonOrder());
        if (newLesson.getDurationMinutes() != null) lesson.setDurationMinutes(newLesson.getDurationMinutes());

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long id, String userEmail, boolean isAdmin) {
        Lesson lesson = getLessonById(id);
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isAdmin && (course.getInstructorId() == null || !course.getInstructorId().equals(user.getId()))) {
            throw new AccessDeniedException("You are not authorized to delete lessons for this course");
        }

        lessonRepository.deleteById(id);

        long totalLessons = lessonRepository.countByCourseId(course.getId());
        course.setLessons((int) totalLessons);
        courseRepository.save(course);
    }
}
