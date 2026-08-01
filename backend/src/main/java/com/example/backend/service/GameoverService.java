package com.example.backend.service;

import com.example.backend.dto.Gameover.GameoverResponse;
import com.example.backend.service.gamestate.session.GameSession;
import org.springframework.stereotype.Service;

import com.example.backend.entity.ScoreRecord;
import java.time.Instant;
import com.example.backend.repository.ScoreRecordRepository;

@Service
public class GameoverService {
    private GameSessionManager gameSessionManager;
    private ScoreRecordRepository scoreRecordRepository;
    private ScoreService scoreService;

    public GameoverService(GameSessionManager gameSessionManager, ScoreRecordRepository scoreRecordRepository, ScoreService scoreService) {
        this.gameSessionManager = gameSessionManager;
        this.scoreRecordRepository = scoreRecordRepository;
        this.scoreService = scoreService;
    }

    public GameoverResponse gameover(String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);
        int score = scoreService.calculateScore(gameSession);
        return new GameoverResponse(score);
    }

    public void registerScore(String sessionId, UserPrincipal principal) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        ScoreRecord scoreRecord = new ScoreRecord(
                principal.getUserId(),
                gameSession.getPlayerState().getName(),
                scoreService.calculateScore(gameSession),
                gameSession.getPlayerState().getLevel(),
                Instant.now()
        );
    
        scoreRecordRepository.save(scoreRecord);
    }


}
