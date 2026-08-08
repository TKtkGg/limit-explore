package com.example.backend.dto.move;

import com.example.backend.domain.SelectedRoute;

public class MoveResponse {
    private SelectedRoute routeType;
    private int remainingSteps;
    private boolean stopped;
    private int currentLaps;
    private int totalLaps;
    private SelectedRoute[] routeOptions;
    private String message;

    public MoveResponse(SelectedRoute routeType, int remainingSteps, boolean stopped, int currentLaps, int totalLaps, SelectedRoute[] routeOptions, String message) {
        this.routeType = routeType;
        this.remainingSteps = remainingSteps;
        this.stopped = stopped;
        this.currentLaps = currentLaps;
        this.totalLaps = totalLaps;
        this.routeOptions = routeOptions;
        this.message = message;
    }

    public SelectedRoute getRouteType() {
        return this.routeType;
    }

    public int getRemainingSteps() {
        return this.remainingSteps;
    }

    public boolean getStopped() {
        return this.stopped;
    }

    public int getCurrentLaps() {
        return this.currentLaps;
    }

    public int getTotalLaps() {
        return this.totalLaps;
    }

    public SelectedRoute[] getRouteOptions() {
        return this.routeOptions;
    }

    public String getMessage() {
        return this.message;
    }
}