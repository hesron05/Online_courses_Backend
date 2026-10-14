package com.example.LearningBackend.controller;

import com.example.LearningBackend.dto.FeedbackRequest;
import com.example.LearningBackend.model.Feedback;
import com.example.LearningBackend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/api/courses/{courseId}/feedback")
    public ResponseEntity<List<Feedback>> getCourseFeedback(@PathVariable Long courseId) {
        return ResponseEntity.ok(feedbackService.getCourseFeedback(courseId));
    }

    @PostMapping("/api/courses/{courseId}/feedback")
    public ResponseEntity<Feedback> submitFeedback(@PathVariable Long courseId,
                                                   @Valid @RequestBody FeedbackRequest request,
                                                   Authentication authentication) {
        Feedback saved = feedbackService.submitFeedback(courseId, request, authentication.getName());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
