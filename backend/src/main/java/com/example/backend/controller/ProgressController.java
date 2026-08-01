package com.example.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.backend.dto.ProgressResponse;
import com.example.backend.service.ProgressService;

@RestController
public class ProgressController {
    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/progress")
    public ProgressResponse getProgress(@RequestHeader("X-Session-Id") String sessionId) {
        return this.progressService.getProgress(sessionId);
    }
    
}
