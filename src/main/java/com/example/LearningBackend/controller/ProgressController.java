package com.example.LearningBackend.controller;

import com.example.LearningBackend.service.LessonProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
public class ProgressController {

    private final LessonProgressService lessonProgressService;

    public ProgressController(LessonProgressService lessonProgressService) {
        this.lessonProgressService = lessonProgressService;
    }

    @PostMapping("/api/lessons/{lessonId}/complete")
    public ResponseEntity<Map<String, Object>> toggleLessonComplete(@PathVariable Long lessonId,
                                                                   Authentication authentication) {
        Map<String, Object> result = lessonProgressService.toggleLessonCompletion(lessonId, authentication.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/courses/{courseId}/progress")
    public ResponseEntity<Map<String, Object>> getCourseProgress(@PathVariable Long courseId,
                                                                 Authentication authentication) {
        Map<String, Object> progress = lessonProgressService.getCourseProgress(courseId, authentication.getName());
        return ResponseEntity.ok(progress);
    }
}
