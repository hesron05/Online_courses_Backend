package com.example.LearningBackend.repository;

import com.example.LearningBackend.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    List<LessonProgress> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    long countByStudentIdAndCourseIdAndCompletedTrue(Long studentId, Long courseId);
    boolean existsByStudentIdAndLessonIdAndCompletedTrue(Long studentId, Long lessonId);
}
