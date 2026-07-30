package com.example.backend.service;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.example.backend.dto.battle.BattleResponse;
import com.example.backend.service.gamestate.card.CardState;
import com.example.backend.service.gamestate.character.CharacterState;
import com.example.backend.service.gamestate.character.EnemyState;
import com.example.backend.service.gamestate.character.EnemyListState;
import com.example.backend.service.gamestate.character.PlayerState;
import com.example.backend.dto.battle.BattleRequest;
import com.example.backend.domain.BattleChoice;
import com.example.backend.service.gamestate.item.ItemState;
import com.example.backend.service.gamestate.item.ItemListState;
import com.example.backend.service.gamestate.session.GameSession;
import com.example.backend.domain.EnemyType;
import com.example.backend.domain.SelectedRoute;

@Service
public class BattleService {
    private Random rand = new Random();
    private List<EnemyState> enemyList;
    private ItemListState itemListState;
    private GameSessionManager gameSessionManager;

    public BattleService(GameSessionManager gameSessionManager, EnemyListState enemyListState, ItemListState itemListState) {
        this.gameSessionManager = gameSessionManager;
        this.enemyList = enemyListState.getEnemyList();
        this.itemListState = itemListState;
    }

    private void setEnemyState(GameSession gameSession) {
        EnemyState template;
        List<EnemyState> selectEnemies = new ArrayList<>();
        for (EnemyState enemy : this.enemyList) {
            for(int i = 0; i < enemy.getSpawnRate(); i++) {
                if (enemy.getEnemyType().equals(EnemyType.BOSS) && gameSession.getMoveState().getRouteType().equals(SelectedRoute.BOSS)) {
                    selectEnemies.add(enemy);
                }
                
                if (enemy.getEnemyType().equals(EnemyType.NORMAL) && gameSession.getMoveState().getRouteType().equals(SelectedRoute.BATTLE)) {
                    if(enemy.getAppearedLevel() <= gameSession.getPlayerState().getLevel()) {
                        selectEnemies.add(enemy);
                    }
                }
            }
        }
        template = selectEnemies.get(rand.nextInt(selectEnemies.size()));
        gameSession.getEnemyState().setName(template.getName());
        gameSession.getEnemyState().setLevel(template.getLevel());
        gameSession.getEnemyState().setMaxHp(template.getMaxHp());
        gameSession.getEnemyState().setHp(template.getHp());
        gameSession.getEnemyState().setAtk(template.getAtk());
        gameSession.getEnemyState().setDef(template.getDef());
        gameSession.getEnemyState().setSpd(template.getSpd());
        gameSession.getEnemyState().setExp(template.getExp());
        gameSession.getEnemyState().setGold(template.getGold());
        gameSession.getEnemyState().setImagePath(template.getImagePath());
    }

    public BattleResponse battleStart(String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        gameSession.getBattleState().reset();
        setEnemyState(gameSession);
        gameSession.getEnemyState().adjustLevel(gameSession.getPlayerState());
        gameSession.getEnemyState().respawn();
        return new BattleResponse("バトル開始！", gameSession.getPlayerState(), gameSession.getEnemyState(), gameSession.getBattleState());
    }

    public BattleResponse battle(BattleRequest request, String sessionId) {
        GameSession gameSession = this.gameSessionManager.getRequiredGameSession(sessionId);

        String message = "";
        gameSession.getBattleState().setPlayerChoice(request.getPlayerChoice());
        gameSession.getBattleState().setEnemyChoice(getRandomEnemyChoice());
        gameSession.getBattleState().setDamageToPlayer(0);
        gameSession.getBattleState().setDamageToEnemy(0);
        if(isPlayerFast(gameSession)) {
            message = playerAction(request, gameSession);
            if(gameSession.getEnemyState().isAlive() && !gameSession.getPlayerState().getIsRun()) {
                message += enemyAction(gameSession);
            }
        } else {
            message = enemyAction(gameSession);
            if(gameSession.getPlayerState().isAlive() && !gameSession.getPlayerState().getIsRun()) {
                message += playerAction(request, gameSession);
            }
        }
        gameSession.getEnemyState().setDefend(false);
        gameSession.getPlayerState().setDefend(false);
        gameSession.getBattleState().setCurrentTurns(gameSession.getBattleState().getCurrentTurns() + 1);
        return new BattleResponse(message, gameSession.getPlayerState(), gameSession.getEnemyState(), gameSession.getBattleState());
    }

    public BattleChoice getRandomEnemyChoice() {
        BattleChoice[] choices = BattleChoice.values();
        return choices[rand.nextInt(choices.length)];
    }

    public boolean isPlayerFast(GameSession gameSession) {
        if(gameSession.getBattleState().getPlayerChoice() == BattleChoice.ITEM || gameSession.getBattleState().getPlayerChoice() == BattleChoice.RUN) {
            return true;
        }
        if(gameSession.getBattleState().getEnemyChoice() == BattleChoice.DEFEND) {
            return false;
        }
        if((gameSession.getPlayerState().getSpd() >= gameSession.getEnemyState().getSpd()) || gameSession.getBattleState().getPlayerChoice() == BattleChoice.DEFEND) {
            return true;
        }
        return false;
    }

    public String playerAction(BattleRequest request, GameSession gameSession){
        if(gameSession.getBattleState().getPlayerChoice() == BattleChoice.ATTACK) {
            return attack(gameSession.getPlayerState(), gameSession.getEnemyState(), gameSession);
        } else if(gameSession.getBattleState().getPlayerChoice() == BattleChoice.DEFEND) {
            return defend(gameSession.getPlayerState());
        } else if(gameSession.getBattleState().getPlayerChoice() == BattleChoice.RUN) {
            return run(gameSession);
        } else if(gameSession.getBattleState().getPlayerChoice() == BattleChoice.ITEM) {
            return item(request.getItemName(), gameSession);
        } else {
            return attack(gameSession.getPlayerState(), gameSession.getEnemyState(), gameSession);
        }
    }

    public String enemyAction(GameSession gameSession){
        if(gameSession.getBattleState().getEnemyChoice() == BattleChoice.ATTACK) {
            return attack(gameSession.getEnemyState(), gameSession.getPlayerState(), gameSession);
        } else if(gameSession.getBattleState().getEnemyChoice() == BattleChoice.DEFEND) {
            return defend(gameSession.getEnemyState());
        } else {
            return attack(gameSession.getEnemyState(), gameSession.getPlayerState(), gameSession);
        }
    }

    public String attack(CharacterState attackerState, CharacterState targetState, GameSession gameSession){
        String message = "";
        double min = 0.8;
		double max = 1.3;
		double randomValue = Math.random() * (max - min) + min;
		int defendMultiplier = targetState.getDefend() ? 1 : 3;
		int damage = (int) ((attackerState.getAtk() - targetState.getDef() / defendMultiplier) * 2 * randomValue); 
        if(attackerState instanceof PlayerState) {
            damage = applyCards(damage, gameSession);
        }
        if(damage < 0) {
            damage = 0;
        }
        if(attackerState instanceof PlayerState) {
            gameSession.getBattleState().setDamageToEnemy(damage);
        } else {
            gameSession.getBattleState().setDamageToPlayer(damage);
        }
        targetState.setHp(targetState.getHp() - damage);
        if(targetState.getHp() < 0) {
            targetState.setHp(0);
            message = result(attackerState.getName(), gameSession);
        }
        
        return attackerState.getName() + "の攻撃！" + damage + "ダメージ！" + message;
    }

    public String defend(CharacterState defenderState){
        defenderState.setDefend(true);
        return defenderState.getName() + "は防御した！";
    }

    public String run(GameSession gameSession){
        gameSession.getPlayerState().setIsRun(true);
        return result("escape", gameSession);
    }

    public String item(String itemName, GameSession gameSession){
        ItemState item = this.itemListState.getItemList().stream().filter(i -> i.getName().equals(itemName)).findFirst().orElse(null);
        if(item == null || gameSession.getPlayerState().getOwnedItems().get(itemName) <= 0) {
            return "そのアイテムは持っていません。";
        }

        if (item.getEffectType().equals("HEAL")) {
            gameSession.getPlayerState().Heal(item.getAmount());
            gameSession.getPlayerState().removeItem(item, 1);
        }
        return gameSession.getPlayerState().getName() + "は" + itemName + "を使用した！";
    }

    public String result(String winnerName, GameSession gameSession) {
        gameSession.getBattleState().setFinished(true);
        if(winnerName != null && winnerName.equals(gameSession.getPlayerState().getName())) {
            int[] reward = applyCards(gameSession.getEnemyState().getGold(), gameSession.getEnemyState().getExp(), gameSession);

            String message = gameSession.getPlayerState().calcExp(reward[1]);
            gameSession.getPlayerState().setGold(gameSession.getPlayerState().getGold() + reward[0]);
            
            return winnerName + "の勝利！ +" + reward[1] + "EXP +" + reward[0] + "Gold " + message;
        } else if(winnerName != null && winnerName.equals(gameSession.getEnemyState().getName())) {
            return gameSession.getPlayerState().getName() + "の敗北！";
        } else {
            return "逃げた！";
        }
    }

    public int applyCards(int damage, GameSession gameSession) {
        for(CardState card : gameSession.getPlayerState().getOwnedCards()) {
            if(card.getName().equals("スライムキラー") && gameSession.getEnemyState().getName().contains("スライム")) {
                damage = (int) (damage * 1.5);
            }
            if(card.getName().equals("ゴブリンキラー") && gameSession.getEnemyState().getName().contains("ゴブリン")) {
                damage = (int) (damage * 1.5);
            }
        }

        return damage;
    }

    public int[] applyCards(int gold, int exp, GameSession gameSession) {
        for(CardState card : gameSession.getPlayerState().getOwnedCards()) {
            if(card.getName().equals("ラッキー2")) {
                gold = (int) (gold * 1.5);
                exp = (int) (exp * 1.5);
            }
        }
        return new int[] {gold, exp};
    }
}