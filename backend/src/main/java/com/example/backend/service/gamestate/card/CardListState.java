package com.example.backend.service.gamestate.card;

import org.springframework.stereotype.Service;

@Service
public class CardListState {
    CardState[] cardList;

    public CardListState() {
        this.cardList = new CardState[] {
            new CardState("装備マスター", "装備の攻撃力を1.5倍にする", 500),
            new CardState("スライムキラー", "「スライム」の名のつく敵に与えるダメージを1.5倍にする", 250),
            new CardState("ゴブリンキラー", "「ゴブリン」の名のつく敵に与えるダメージを1.5倍にする", 250),
            new CardState("ラッキー", "ステータス宝箱から得る報酬を2倍にする", 800),
            new CardState("ラッキー2", "勝利時の報酬を1.5倍にする", 800),
            new CardState("アディショナルスコア", "得るスコアが1.5倍になる", 1000),
        };
    }   

    public CardState[] getCardList() {
        return this.cardList;
    }
}
