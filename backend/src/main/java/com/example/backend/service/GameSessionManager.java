package com.example.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import com.example.backend.service.gamestate.session.GameSession;
import com.example.backend.service.gamestate.character.PlayerState;
import com.example.backend.service.gamestate.MoveState;
import com.example.backend.service.gamestate.BattleState;
import com.example.backend.service.gamestate.treasure.TreasureState;
import com.example.backend.service.gamestate.shop.ShopState;
import com.example.backend.service.gamestate.equipment.EquipmentListState;
import com.example.backend.exception.SessionNotFoundException;
import com.example.backend.service.gamestate.character.EnemyState;
import com.example.backend.domain.EnemyType;

@Service
public class GameSessionManager {
    private Map<String, GameSession> gameSessions;

    public GameSessionManager() {
        this.gameSessions = new ConcurrentHashMap<>();
    }

    public GameSession createGameSession(String sessionId) {
        GameSession gameSession = new GameSession(sessionId, new PlayerState(new EquipmentListState()), new MoveState(), new BattleState(), new TreasureState(), new ShopState(), new EnemyState("スライム", 1, 100, 100, 10, 10, 10, 0, 100, "/img/enemy/slime.png", 1, 10, EnemyType.NORMAL));
        gameSessions.put(sessionId, gameSession);
        return gameSession;
    }

    public GameSession getGameSession(String sessionId) {
        return gameSessions.get(sessionId);
    }

    public void removeGameSession(String sessionId) {
        gameSessions.remove(sessionId);
    }

    public GameSession getRequiredGameSession(String sessionId) {
        GameSession gameSession = this.getGameSession(sessionId);
        if (gameSession == null) {
            throw new SessionNotFoundException();
        }
        return gameSession;
    }

    public void putGameSession(GameSession gameSession) {
        gameSessions.put(gameSession.getSessionId(), gameSession);
    }
}
