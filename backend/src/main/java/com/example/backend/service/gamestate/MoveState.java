package com.example.backend.service.gamestate;

import com.example.backend.domain.SelectedRoute;

public class MoveState {
    private int remainingSteps;
    private boolean stopped;
    private int currentLaps;
    private final int totalLaps = 3;
    private boolean cleared;
    private SelectedRoute routeType;
    private SelectedRoute[] routeOptions;
    private SelectedRoute[] randomRouteOptions;

    public MoveState() {
        this.remainingSteps = 25;
        this.stopped = false;
        this.currentLaps = 1;
        this.cleared = false;
        this.routeType = null;
        this.routeOptions = new SelectedRoute[] {SelectedRoute.BATTLE, SelectedRoute.TREASURE, SelectedRoute.REST, SelectedRoute.CARD, SelectedRoute.SHOP};
        this.randomRouteOptions = null;
    }
    public int getRemainingSteps() {
        return remainingSteps;
    }
    public boolean isStopped() {
        return stopped;
    }
    public int getCurrentLaps() {
        return currentLaps;
    }
    public int getTotalLaps() {
        return totalLaps;
    }
    public boolean getCleared() {
        return cleared;
    }
    public SelectedRoute getRouteType() {
        return routeType;
    }
    public SelectedRoute[] getRouteOptions() {
        return routeOptions;
    }
    public SelectedRoute[] getRandomRouteOptions() {
        return randomRouteOptions;
    }

    public void setRemainingSteps(int remainingSteps) {
        this.remainingSteps = remainingSteps;
    }
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    public void setCurrentLaps(int currentLaps) {
        this.currentLaps = currentLaps;
    }
    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }
    public void setRouteType(SelectedRoute routeType) {
        this.routeType = routeType;
    }
    public void setRandomRouteOptions(SelectedRoute[] randomRouteOptions) {
        this.randomRouteOptions = randomRouteOptions;
    }
}