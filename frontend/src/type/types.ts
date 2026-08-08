import { FlashType } from "@/lib/effectPaths";
import { SpriteEffectType } from "@/lib/effectPaths";

export type CardState = {
    name: string;
    text: string;
    price: number;
}

export type EquipmentState = {
    name: string;
    atk: number;
    price: number;
    chance: number;
}

export type MerchandiseState = {
    name: string;
    price: number;
    type: string;
}

export type ShopState = {
    gold: number;
    display: MerchandiseState[];
    message: string;
}

export type EnemyState = {
    name: string;
    level: number;
    maxHp: number;
    hp: number;
    atk: number;
    totalAtk: number;
    def: number;
    spd: number;
    exp: number;
    gold: number;
    imagePath: string;
    enemyType: "NORMAL" | "BOSS";
}

export type StatusState = {
    name: string;
    level: number;
    maxHp: number;
    hp: number;
    atk: number;
    totalAtk: number;
    def: number;
    spd: number;
    exp: number;
    gold: number;
    nextLevelExp: number;
    equipment: EquipmentState;
    ownedEquipmentList: EquipmentState[];
    ownedCards: CardState[];
    ownedItems: { [key: string]: number };
}

export enum BattleChoice {
    ATTACK = "ATTACK",
    DEFEND = "DEFEND",
    ITEM = "ITEM",
    RUN = "RUN",
}

export type BattleState = {
    message: string;
    playerState: StatusState;
    enemyState: EnemyState;
    battleState: {
        currentTurns: number;
        damageToPlayer: number;
        damageToEnemy: number;
        healAmount: number;
        finished: boolean;
        playerChoice: BattleChoice;
        enemyChoice: BattleChoice;
    };
}

export type BattleEffect =
    | { kind: "flash"; type: FlashType; target: "player" | "enemy"; key: number }
    | { kind: "sprite"; type: SpriteEffectType; target: "player" | "enemy"; key: number }
    | null;

export type RankingResponse = {
    userId: number;
    playerName: string;
    score: number;
    level: number;
    playedAt: Date;
}