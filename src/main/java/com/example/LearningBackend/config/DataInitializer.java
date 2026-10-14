package com.example.LearningBackend.config;

import com.example.LearningBackend.model.Course;
import com.example.LearningBackend.model.Enrollment;
import com.example.LearningBackend.model.Lesson;
import com.example.LearningBackend.model.LessonProgress;
import com.example.LearningBackend.model.User;
import com.example.LearningBackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CourseRepository courseRepository,
                           LessonRepository lessonRepository,
                           EnrollmentRepository enrollmentRepository,
                           LessonProgressRepository lessonProgressRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed users if none exist
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@learnhub.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);

            User instructor = new User();
            instructor.setName("Dr. Priya Sharma");
            instructor.setEmail("priya@learnhub.com");
            instructor.setPassword(passwordEncoder.encode("instructor123"));
            instructor.setRole("instructor");
            instructor.setCreatedAt(LocalDateTime.now());
            User savedInstructor = userRepository.save(instructor);

            User student = new User();
            student.setName("Rahul Kumar");
            student.setEmail("rahul@learnhub.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setRole("student");
            student.setCreatedAt(LocalDateTime.now());
            User savedStudent = userRepository.save(student);

            // Seed courses if none exist
            if (courseRepository.count() == 0) {
                Course c1 = new Course();
                c1.setTitle("Python Programming Masterclass");
                c1.setDescription("Learn Python from core fundamentals to advanced data structures and OOP concepts.");
                c1.setInstructor(savedInstructor.getName());
                c1.setInstructorId(savedInstructor.getId());
                c1.setCategory("Programming");
                c1.setDifficulty("Beginner");
                c1.setPrice("Free");
                c1.setRating(4.9);
                c1.setLessons(5);
                c1.setPublished(true);
                c1.setCreatedAt(LocalDateTime.now());
                Course savedC1 = courseRepository.save(c1);

                Course c2 = new Course();
                c2.setTitle("Full Stack Web Development");
                c2.setDescription("Build modern, responsive full-stack applications using React, Node, and RESTful architectures.");
                c2.setInstructor(savedInstructor.getName());
                c2.setInstructorId(savedInstructor.getId());
                c2.setCategory("Development");
                c2.setDifficulty("Intermediate");
                c2.setPrice("Free");
                c2.setRating(4.8);
                c2.setLessons(4);
                c2.setPublished(true);
                c2.setCreatedAt(LocalDateTime.now());
                Course savedC2 = courseRepository.save(c2);

                Course c3 = new Course();
                c3.setTitle("Database Systems & MySQL Design");
                c3.setDescription("Master relational databases, schema normalization, indexing, transactions, and SQL queries.");
                c3.setInstructor(savedInstructor.getName());
                c3.setInstructorId(savedInstructor.getId());
                c3.setCategory("Database");
                c3.setDifficulty("Beginner");
                c3.setPrice("Free");
                c3.setRating(4.7);
                c3.setLessons(4);
                c3.setPublished(true);
                c3.setCreatedAt(LocalDateTime.now());
                courseRepository.save(c3);

                Course c4 = new Course();
                c4.setTitle("Java & Spring Boot Microservices");
                c4.setDescription("Enterprise software engineering with Java 21, Spring Boot, Spring Security, and Hibernate.");
                c4.setInstructor(savedInstructor.getName());
                c4.setInstructorId(savedInstructor.getId());
                c4.setCategory("Programming");
                c4.setDifficulty("Advanced");
                c4.setPrice("Free");
                c4.setRating(4.9);
                c4.setLessons(4);
                c4.setPublished(true);
                c4.setCreatedAt(LocalDateTime.now());
                courseRepository.save(c4);

                // Add lessons to c1 (Python)
                Lesson l1 = lessonRepository.save(new Lesson(savedC1.getId(), "Introduction to Python and Setup", "Setting up Python 3, IDE environment, and running your first script.", "https://www.youtube.com/embed/_uQrJ0TkZlc", 1));
                Lesson l2 = lessonRepository.save(new Lesson(savedC1.getId(), "Variables, Data Types and Operators", "Deep dive into numbers, strings, booleans, and arithmetic operations.", "https://www.youtube.com/embed/_uQrJ0TkZlc", 2));
                lessonRepository.save(new Lesson(savedC1.getId(), "Control Flow and Loops", "Master if-elif-else statements, while loops, and for range iterations.", "https://www.youtube.com/embed/_uQrJ0TkZlc", 3));
                lessonRepository.save(new Lesson(savedC1.getId(), "Functions, Modules and Scope", "Creating modular, reusable functions with default and keyword arguments.", "https://www.youtube.com/embed/_uQrJ0TkZlc", 4));
                lessonRepository.save(new Lesson(savedC1.getId(), "Object-Oriented Programming in Python", "Classes, inheritance, encapsulation, polymorphism, and dunder methods.", "https://www.youtube.com/embed/_uQrJ0TkZlc", 5));

                // Add lessons to c2 (Web Dev)
                lessonRepository.save(new Lesson(savedC2.getId(), "HTML5 Semantics and Modern Layouts", "Semantic structure, forms, tables, and accessibility standards.", "https://www.youtube.com/embed/nu_pCVPKzTk", 1));
                lessonRepository.save(new Lesson(savedC2.getId(), "CSS3 Styling, Flexbox, and Grid", "Creating responsive modern web interfaces with flexbox and css grid.", "https://www.youtube.com/embed/nu_pCVPKzTk", 2));
                lessonRepository.save(new Lesson(savedC2.getId(), "JavaScript ES6+ Fundamentals", "Arrow functions, destructuring, promises, async/await, and fetch API.", "https://www.youtube.com/embed/nu_pCVPKzTk", 3));
                lessonRepository.save(new Lesson(savedC2.getId(), "React Component Architecture", "Components, props, useState, useEffect, and client-side routing.", "https://www.youtube.com/embed/nu_pCVPKzTk", 4));

                // Enroll student in Python course with 2 completed lessons
                Enrollment e1 = new Enrollment(savedStudent.getId(), savedC1.getId());
                e1.setProgressPercentage(40);
                e1.setCompleted(false);
                e1.setEnrolledAt(LocalDateTime.now().minusDays(3));
                enrollmentRepository.save(e1);

                lessonProgressRepository.save(new LessonProgress(savedStudent.getId(), l1.getId(), savedC1.getId(), true));
                lessonProgressRepository.save(new LessonProgress(savedStudent.getId(), l2.getId(), savedC1.getId(), true));

                // Enroll student in Web Dev course
                Enrollment e2 = new Enrollment(savedStudent.getId(), savedC2.getId());
                e2.setProgressPercentage(0);
                e2.setCompleted(false);
                e2.setEnrolledAt(LocalDateTime.now().minusDays(1));
                enrollmentRepository.save(e2);
            }
        }
    }
}
