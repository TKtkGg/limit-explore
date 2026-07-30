package com.example.backend.service.gamestate.character;

import com.example.backend.domain.EnemyType;

public class EnemyState extends CharacterState {
    String imagePath;
    int appearedLevel;
    int spawnRate;
    EnemyType enemyType;

    public EnemyState(String name, int level, int maxHp, int hp, int atk, int def, int spd, int exp, int gold, String imagePath, int appearedLevel, int spawnRate, EnemyType enemyType) {
        super(name, level, maxHp, hp, atk, def, spd, exp, gold);
        this.imagePath = imagePath;
        this.appearedLevel = appearedLevel;
        this.spawnRate = spawnRate;
        this.enemyType = enemyType;
    }

    public void respawn() {
        this.setHp(this.getMaxHp());
    }

    public void adjustLevel(PlayerState p) {
		double playerAvgStatus = (p.getMaxHp() / 10 + p.getAtk() + p.getDef() + p.getSpd()) / 4 + p.getOwnedCards().size() * 5;
		double enemyAvgStatus = (this.getMaxHp() / 10 + this.getAtk() + this.getDef() + this.getSpd()) / 4;
		int levelDifference = (int) ((playerAvgStatus - enemyAvgStatus) / 5);
		
		this.setLevel(this.getLevel() + levelDifference);
		this.setMaxHp(this.getMaxHp() + levelDifference * 10);
		this.setAtk(this.getAtk() + levelDifference);
		this.setDef(this.getDef() + levelDifference);
		this.setSpd(this.getSpd() + levelDifference);
        this.setExp(this.getExp() + (this.getExp() / 5 * this.getLevel()));
        this.setGold(this.getGold() + (this.getGold() / 5 * this.getLevel()));
	}

    public String getImagePath() {
        return this.imagePath;
    }

    public int getAppearedLevel() {
        return this.appearedLevel;
    }

    public int getSpawnRate() {
        return this.spawnRate;
    }

    public EnemyType getEnemyType() {
        return this.enemyType;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setAppearedLevel(int appearedLevel) {
        this.appearedLevel = appearedLevel;
    }

    public void setSpawnRate(int spawnRate) {
        this.spawnRate = spawnRate;
    }

    public void setEnemyType(EnemyType enemyType) {
        this.enemyType = enemyType;
    }
}
