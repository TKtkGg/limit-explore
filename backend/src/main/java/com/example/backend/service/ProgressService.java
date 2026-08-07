package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.service.gamestate.session.GameSession;
import com.example.backend.dto.ProgressResponse;

@Service
public class ProgressService {
    private GameSessionManager gameSessionManager;
    private ScoreService scoreService;

    public ProgressService(GameSessionManager gameSessionManager, ScoreService scoreService) {
        this.gameSessionManager = gameSessionManager;
        this.scoreService = scoreService;
    }

    public ProgressResponse getProgress(String sessionId) {
        GameSession gameSession = gameSessionManager.getRequiredGameSession(sessionId);
        int score = scoreService.calculateScore(gameSession);
        return new ProgressResponse(score, gameSession.getMoveState().getCleared(), gameSession.getPlayerState());
    }

    public void continueProgress(String sessionId) {
        GameSession gameSession = gameSessionManager.getRequiredGameSession(sessionId);
        gameSession.getMoveState().setCurrentLaps(gameSession.getMoveState().getCurrentLaps() + 1);
    }
}
