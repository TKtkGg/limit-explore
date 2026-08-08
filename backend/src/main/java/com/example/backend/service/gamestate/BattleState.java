package com.example.backend.service.gamestate;

import com.example.backend.domain.BattleChoice;

public class BattleState {
    int currentTurns;
    int damageToPlayer;
    int damageToEnemy;
    int healAmount;
    boolean finished;
    BattleChoice playerChoice;
    BattleChoice enemyChoice;
    public BattleState() {
        this.currentTurns = 0;
        this.damageToPlayer = 0;
        this.damageToEnemy = 0;
        this.healAmount = 0;
        this.finished = false;
        this.playerChoice = null;
        this.enemyChoice = null;
    }
    public int getCurrentTurns() {
        return currentTurns;
    }
    public int getDamageToPlayer() {
        return damageToPlayer;
    }
    public int getDamageToEnemy() {
        return damageToEnemy;
    }
    public int getHealAmount() {
        return healAmount;
    }
    public boolean getFinished() {
        return finished;
    }
    public BattleChoice getPlayerChoice() {
        return playerChoice;
    }
    public BattleChoice getEnemyChoice() {
        return enemyChoice;
    }

    public void setCurrentTurns(int currentTurns) {
        this.currentTurns = currentTurns;
    }
    public void setDamageToPlayer(int damageToPlayer) {
        this.damageToPlayer = damageToPlayer;
    }
    public void setDamageToEnemy(int damageToEnemy) {
        this.damageToEnemy = damageToEnemy;
    }
    public void setHealAmount(int healAmount) {
        this.healAmount = healAmount;
    }
    public void setPlayerChoice(BattleChoice playerChoice) {
        this.playerChoice = playerChoice;
    }
    public void setEnemyChoice(BattleChoice enemyChoice) {
        this.enemyChoice = enemyChoice;
    }
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void reset() {
        this.currentTurns = 0;
        this.damageToPlayer = 0;
        this.damageToEnemy = 0;
        this.healAmount = 0;
        this.finished = false;
        this.playerChoice = null;
        this.enemyChoice = null;
    }
}
