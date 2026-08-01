package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.service.gamestate.session.GameSession;
import com.example.backend.service.gamestate.card.CardState;

@Service
public class ScoreService {
    public int calculateScore(GameSession gameSession) {
        int score = gameSession.getPlayerState().getLevel() * 100 
                    + gameSession.getPlayerState().getOwnedEquipmentList().size() * 100 
                    + gameSession.getPlayerState().getOwnedCards().size() * 70
                    + gameSession.getPlayerState().getMaxHp() * 3
                    + gameSession.getPlayerState().getAtk() * 10
                    + gameSession.getPlayerState().getDef() * 10
                    + gameSession.getPlayerState().getSpd() * 10;
        score = applyCards(score, gameSession);
        return score;
    }

    private int applyCards(int score, GameSession gameSession) {
        for(CardState card : gameSession.getPlayerState().getOwnedCards()) {
            if(card.getName().equals("アディショナルスコア")) {
                score = (int) (score * 1.5);
            }
        }
        return score;
    }
}
