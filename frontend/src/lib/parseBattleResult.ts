export type BattleResultDisplay = {
    title: string;
    exp: number | null;
    gold: number | null;
    levelFrom: number | null;
    levelTo: number | null;
    backPath: "/explore" | "/gameover" | "/card";
};

export function parseBattleResult(
    message: string,
    playerLevel: number,
    playerHp: number,
    enemyType: "NORMAL" | "BOSS"
): BattleResultDisplay | null {
    if (message.includes("逃げた")) {
        return {
            title: "逃走",
            exp: null,
            gold: null,
            levelFrom: null,
            levelTo: null,
            backPath: "/explore",
        };
    }

    if (message.includes("敗北")) {
        return {
            title: "敗北",
            exp: null,
            gold: null,
            levelFrom: null,
            levelTo: null,
            backPath: playerHp > 0 ? "/explore" : "/gameover",
        };
    }

    const victoryMatch = message.match(/の勝利！\s*\+(\d+)EXP\s*\+(\d+)Gold/i);
    if (victoryMatch) {
        const levelUpCount = (message.match(/レベルアップ！/g) ?? []).length;
        return {
            title: "勝利",
            exp: Number(victoryMatch[1]),
            gold: Number(victoryMatch[2]),
            levelFrom: levelUpCount > 0 ? playerLevel - levelUpCount : null,
            levelTo: levelUpCount > 0 ? playerLevel : null,
            backPath: enemyType === "BOSS" ? "/card" : "/explore",
        };
    }

    return null;
}
