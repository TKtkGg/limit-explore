package com.example.backend.dto;

public class ProgressResponse {
    private int score;
    private boolean cleared;

    public ProgressResponse(int score, boolean cleared) {
        this.score = score;
        this.cleared = cleared;
    }

    public int getScore() {
        return score;
    }

    public boolean isCleared() {
        return cleared;
    }

}
