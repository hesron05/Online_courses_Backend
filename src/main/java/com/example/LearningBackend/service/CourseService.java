package com.example.LearningBackend.service;

import com.example.LearningBackend.exception.BadRequestException;
import com.example.LearningBackend.exception.ResourceNotFoundException;
import com.example.LearningBackend.model.Course;
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
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public CourseService(CourseRepository courseRepository,
                         UserRepository userRepository,
                         LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    public List<Course> getAllCourses() {
        List<Course> published = courseRepository.findByPublishedTrue();
        if (published.isEmpty()) {
            return courseRepository.findAll();
        }
        return published;
    }

    public List<Course> getAllCoursesAdmin() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    public List<Course> getInstructorCourses(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        return courseRepository.findByInstructorId(instructor.getId());
    }

    public Course createCourse(Course course, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        course.setInstructorId(instructor.getId());
        if (course.getInstructor() == null || course.getInstructor().isBlank()) {
            course.setInstructor(instructor.getName());
        }
        if (course.getLessons() == null) {
            course.setLessons(0);
        }
        if (course.getRating() == null) {
            course.setRating(5.0);
        }
        if (course.getPublished() == null) {
            course.setPublished(true);
        }
        if (course.getCreatedAt() == null) {
            course.setCreatedAt(LocalDateTime.now());
        }
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course newCourse, String userEmail, boolean isAdmin) {
        Course course = getCourseById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isAdmin && (course.getInstructorId() == null || !course.getInstructorId().equals(user.getId()))) {
            throw new AccessDeniedException("You are not authorized to update this course");
        }

        if (newCourse.getTitle() != null) course.setTitle(newCourse.getTitle());
        if (newCourse.getDescription() != null) course.setDescription(newCourse.getDescription());
        if (newCourse.getCategory() != null) course.setCategory(newCourse.getCategory());
        if (newCourse.getDifficulty() != null) course.setDifficulty(newCourse.getDifficulty());
        if (newCourse.getPrice() != null) course.setPrice(newCourse.getPrice());
        if (newCourse.getThumbnail() != null) course.setThumbnail(newCourse.getThumbnail());
        if (newCourse.getPublished() != null) course.setPublished(newCourse.getPublished());
        if (newCourse.getLessons() != null) course.setLessons(newCourse.getLessons());

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id, String userEmail, boolean isAdmin) {
        Course course = getCourseById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isAdmin && (course.getInstructorId() == null || !course.getInstructorId().equals(user.getId()))) {
            throw new AccessDeniedException("You are not authorized to delete this course");
        }

        lessonRepository.deleteByCourseId(id);
        courseRepository.deleteById(id);
    }
}