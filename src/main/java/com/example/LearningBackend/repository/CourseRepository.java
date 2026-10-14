package com.example.LearningBackend.repository;

import com.example.LearningBackend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByPublishedTrue();
    List<Course> findByCategory(String category);
    long countByInstructorId(Long instructorId);
}