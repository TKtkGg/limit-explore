package com.example.backend.dto;

import com.example.backend.service.gamestate.character.PlayerState;

public class ProgressResponse {
    private int score;
    private boolean cleared;
    private PlayerState playerState;

    public ProgressResponse(int score, boolean cleared, PlayerState playerState) {
        this.score = score;
        this.cleared = cleared;
        this.playerState = playerState;
    }

    public int getScore() {
        return score;
    }

    public boolean isCleared() {
        return cleared;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

}
