package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.domain.SelectedRoute;
import com.example.backend.dto.move.MoveRequest;
import com.example.backend.dto.move.MoveResponse;
import com.example.backend.exception.GameStoppedException;
import com.example.backend.service.gamestate.MoveState;
import com.example.backend.service.gamestate.session.GameSession;

import java.util.Random;
import java.util.Arrays;

@Service
public class MoveService {
    Random rand = new Random();

    private CardService cardService;
    private GameSessionManager gameSessionManager;
    public MoveService(CardService cardService, GameSessionManager gameSessionManager) {
        this.cardService = cardService;
        this.gameSessionManager = gameSessionManager;
    }

    private void moveAbstract(MoveRequest request, MoveState moveState) {
        moveState.setRouteType(request.getRouteType());
        if(!moveState.isStopped()) {
            moveState.setRemainingSteps(moveState.getRemainingSteps() - 1);
        } else {
            throw new GameStoppedException("ゲームが停止しました。");
        } 
        if(moveState.getRemainingSteps() <= 0) {
            moveState.setRemainingSteps(25);
            if (moveState.getCurrentLaps() > moveState.getTotalLaps()) {
                moveState.setStopped(true);
            }
        }

    }

    public MoveResponse move(MoveRequest request, String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        this.moveAbstract(request, gameSession.getMoveState());
        return new MoveResponse(gameSession.getMoveState().getRouteType(), gameSession.getMoveState().getRemainingSteps(), gameSession.getMoveState().isStopped(), gameSession.getMoveState().getCurrentLaps(), gameSession.getMoveState().getTotalLaps(), this.getRandomRouteOptions(gameSession), "");
    }

    public MoveResponse restart(String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        gameSession.getPlayerState().init("No Name");
        
        gameSession.getMoveState().setStopped(false);
        gameSession.getMoveState().setCurrentLaps(1);
        gameSession.getMoveState().setRouteType(null);

        return new MoveResponse(gameSession.getMoveState().getRouteType(), gameSession.getMoveState().getRemainingSteps(), gameSession.getMoveState().isStopped(), gameSession.getMoveState().getCurrentLaps(), gameSession.getMoveState().getTotalLaps(), this.getRandomRouteOptions(gameSession), "");
    }

    public MoveResponse getCurrentMoveState(String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);
        
        SelectedRoute[] options = gameSession.getMoveState().getRandomRouteOptions();
        if(shouldRegenerate(options, gameSession)) {
            options = this.getRandomRouteOptions(gameSession);
        }
        return new MoveResponse(gameSession.getMoveState().getRouteType(), gameSession.getMoveState().getRemainingSteps(), gameSession.getMoveState().isStopped(), gameSession.getMoveState().getCurrentLaps(), gameSession.getMoveState().getTotalLaps(), options, "");
    }

    public SelectedRoute[] getRandomRouteOptions(GameSession gameSession) {
        SelectedRoute[] route = gameSession.getMoveState().getRouteOptions();
        if(this.cardService.getUnownedCards(gameSession).isEmpty()) {
            SelectedRoute[] newRoute = new SelectedRoute[route.length - 1];
            int index = 0;
            for(SelectedRoute r : route) {
                if(r == SelectedRoute.CARD) {
                    continue;
                }
                newRoute[index] = r;
                index++;
            }
            route = newRoute;
        }
        SelectedRoute[] options = {route[rand.nextInt(route.length)], route[rand.nextInt(route.length)], route[rand.nextInt(route.length)]};
        if (gameSession.getMoveState().getRemainingSteps() == 1) {
            options = new SelectedRoute[] {SelectedRoute.BOSS, SelectedRoute.BOSS, SelectedRoute.BOSS};
        }
        gameSession.getMoveState().setRandomRouteOptions(options);

        
        return options;
    }

    public MoveResponse rest(MoveRequest request, String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        this.moveAbstract(request, gameSession.getMoveState());
        int healAmount = gameSession.getPlayerState().Heal(100);
        return new MoveResponse(gameSession.getMoveState().getRouteType(), gameSession.getMoveState().getRemainingSteps(), gameSession.getMoveState().isStopped(), gameSession.getMoveState().getCurrentLaps(), gameSession.getMoveState().getTotalLaps(), this.getRandomRouteOptions(gameSession), "休んで" + healAmount + "回復した！");
    }

    private boolean shouldRegenerate(SelectedRoute[] options, GameSession gameSession) {
        if (options == null) return true;
        if (Arrays.asList(options).contains(SelectedRoute.BOSS) && gameSession.getMoveState().getRemainingSteps() == 1) return true;
        return Arrays.asList(options).contains(SelectedRoute.CARD) && this.cardService.getUnownedCards(gameSession).isEmpty();
    }
}