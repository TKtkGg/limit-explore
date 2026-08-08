package com.example.backend.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;

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

    @PostMapping("/progress/continue")
    public Map<String, String> continueProgress(@RequestHeader("X-Session-Id") String sessionId) {
        this.progressService.continueProgress(sessionId);
        return Map.of("message", "ok");
    }
}
