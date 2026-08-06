package com.example.backend.service.gamestate.card;

import org.springframework.stereotype.Service;

@Service
public class SpecialCardListState {
    CardState[] specialCardList;

    public SpecialCardListState() {
        this.specialCardList = new CardState[] {
            new CardState("スーパーパワー", "ステータスを永久に1.5倍にする", 1000),
            new CardState("リッチ", "即座に3000Gを入手する", 3000),
            new CardState("クリティカル！", "確率で敵に与えるダメージが2倍になる", 1000),
        };
    }   

    public CardState[] getSpecialCardList() {
        return this.specialCardList;
    }
}
