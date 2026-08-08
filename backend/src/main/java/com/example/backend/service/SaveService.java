package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.entity.SaveData;
import com.example.backend.repository.SaveDataRepository;
import com.example.backend.service.gamestate.session.GameSaveSnapshot;
import com.example.backend.service.gamestate.session.GameSession;
import com.example.backend.service.gamestate.BattleState;
import com.example.backend.service.gamestate.treasure.TreasureState;
import com.example.backend.service.gamestate.shop.ShopState;
import com.example.backend.service.gamestate.character.EnemyState;
import com.example.backend.domain.EnemyType;

import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
public class SaveService {
    private final ObjectMapper objectMapper;
    private final GameSessionManager gameSessionManager;
    private final SaveDataRepository saveDataRepository;

    public SaveService(ObjectMapper objectMapper, GameSessionManager gameSessionManager, SaveDataRepository saveDataRepository) {
        this.objectMapper = objectMapper;
        this.gameSessionManager = gameSessionManager;
        this.saveDataRepository = saveDataRepository;
    }

    public String toJson(GameSaveSnapshot snapshot) throws Exception {
        return objectMapper.writeValueAsString(snapshot);
    }

    public GameSaveSnapshot fromJson(String json) throws Exception {
        return objectMapper.readValue(json, GameSaveSnapshot.class);
    }

    public void deleteByUserId(Integer userId) {
        SaveData saveData = saveDataRepository.findByUserId(userId);
        if (saveData != null) {
            saveDataRepository.delete(saveData);
        }
    }

    public void save(String sessionId, Integer userId) throws Exception {
        GameSession gameSession = gameSessionManager.getRequiredGameSession(sessionId);

        GameSaveSnapshot snapshot = new GameSaveSnapshot();
        snapshot.setPlayerState(gameSession.getPlayerState());
        snapshot.setMoveState(gameSession.getMoveState());

        String json = toJson(snapshot);
        SaveData saveData = saveDataRepository.findByUserId(userId);
        if (saveData == null) {
            saveData = new SaveData(userId, json, Instant.now());
        } else {
            saveData.setSaveData(json);
            saveData.setUpdatedAt(Instant.now());
        }
        saveDataRepository.save(saveData);
    }

    public String load(Integer userId) throws Exception {
        SaveData saveData = saveDataRepository.findByUserId(userId);
        if (saveData == null) {
            return null;
        }
        String json = saveData.getSaveData();
        GameSaveSnapshot snapshot = fromJson(json);
        String newSessionId = UUID.randomUUID().toString();

        GameSession gameSession = new GameSession(
            newSessionId,
            snapshot.getPlayerState(),
            snapshot.getMoveState(), 
            new BattleState(), 
            new TreasureState(), 
            new ShopState(), 
            new EnemyState("スライム", 1, 100, 100, 10, 10, 10, 0, 100, "/img/enemy/slime.png", 1, 10, EnemyType.NORMAL));
        
        gameSession.setSessionId(newSessionId);

        gameSessionManager.putGameSession(gameSession);
        return newSessionId;
    }
}
