package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.example.backend.dto.card.CardRequest;
import com.example.backend.dto.card.CardResponse;
import com.example.backend.dto.card.CardsResponse;
import com.example.backend.service.gamestate.card.CardListState;
import com.example.backend.service.gamestate.card.CardState;
import com.example.backend.service.gamestate.card.SpecialCardListState;
import com.example.backend.service.gamestate.session.GameSession;

@Service
public class CardService {
    private CardListState cardListState;
    private GameSessionManager gameSessionManager;
    private SpecialCardListState specialCardListState;

    public CardService(GameSessionManager gameSessionManager, CardListState cardListState, SpecialCardListState specialCardListState) {
        this.gameSessionManager = gameSessionManager;
        this.cardListState = cardListState;
        this.specialCardListState = specialCardListState;
    }

    public CardResponse showCards(String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        gameSession.getCardLineup().clear();
        gameSession.getCardDisplay().clear();

        gameSession.getCardLineup().addAll(getUnownedCards(gameSession));
        Collections.shuffle(gameSession.getCardLineup());

        gameSession.getCardDisplay().addAll(gameSession.getCardLineup().subList(0, 3 < gameSession.getCardLineup().size() ? 3 : gameSession.getCardLineup().size()));
        
        return new CardResponse(gameSession.getCardDisplay(), null, gameSession.getMoveState().isDefeatedBoss());
    }

    public CardResponse chooseCard(CardRequest request, String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        gameSession.getPlayerState().addCard(request.getChosenCard());
        gameSession.getMoveState().setDefeatedBoss(false);
        return new CardResponse(gameSession.getCardDisplay(), request.getChosenCard(), gameSession.getMoveState().isDefeatedBoss());
    }

    public List<CardState> getUnownedCards(GameSession gameSession) {
        if (gameSession.getMoveState().isDefeatedBoss()) {
            return Arrays.stream(this.specialCardListState.getSpecialCardList()).filter(card -> !gameSession.getPlayerState().getOwnedCards().contains(card)).collect(Collectors.toList());
        }
        return Arrays.stream(this.cardListState.getCardList()).filter(card -> !gameSession.getPlayerState().getOwnedCards().contains(card)).collect(Collectors.toList());
    }

    public CardsResponse showOwnedCards(String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        return new CardsResponse(gameSession.getPlayerState().getOwnedCards());
    }
}
