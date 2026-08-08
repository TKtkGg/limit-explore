package com.example.backend.dto.card;

import java.util.List;

import com.example.backend.service.gamestate.card.CardState;

public class CardResponse {
    private List<CardState> display;
    private CardState chosenCard;
    private boolean isDefeatedBoss;

    public CardResponse(List<CardState> display, CardState chosenCard, boolean isDefeatedBoss) {
        this.display = display;
        this.chosenCard = chosenCard;
        this.isDefeatedBoss = isDefeatedBoss;
    }

    public List<CardState> getDisplay() {
        return this.display;
    }

    public CardState getChosenCard() {
        return this.chosenCard;
    }

    public boolean isDefeatedBoss() {
        return this.isDefeatedBoss;
    }
}
