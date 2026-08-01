"use client";

import { useEffect, useRef, useState } from "react";
import { apiGet, apiPost } from "@/lib/apiClient";
import { BattleState, BattleChoice } from "@/type/types";
import { useRouter } from "next/navigation";
import { ErrorAlert } from "@/components/atoms/ErrorAlert";
import { BattleEnemyDisplay } from "@/components/molecules/BattleEnemyDisplay";
import { BattleCommandBox } from "@/components/molecules/BattleCommandBox";
import { BattleMessageBox } from "@/components/molecules/BattleMessageBox";
import { BattleResultModal } from "@/components/molecules/BattleResultModal";
import { parseBattleResult } from "@/lib/parseBattleResult";
import { getItemHealAmount } from "@/lib/itemHealAmount";
import { messageDivision } from "@/lib/messageDivision";
import { isPlayerFast } from "@/lib/isPlayerFast";
import { sleep } from "@/lib/sleepHelper";
import { useAudio } from "@/components/providers/AudioProvider";
import { BGM, SFX } from "@/lib/audioPaths";
import { BACKGROUNDS } from "@/lib/imagePaths";
import { FlashType, SpriteEffectType } from "@/lib/effectPaths";
import { BattleEffect } from "@/type/types";
import { useRequireSession } from "@/hooks/useRequireSession";
import { useRequireActiveGame } from "@/hooks/useRequireActiveGame";
import { markGameFinished } from "@/lib/session";


type DamageFloater = {
    target: "player" | "enemy";
    value: number;
    key: number;
}

export default function BattlePage() {
    const [battleState, setBattleState] = useState<BattleState | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [itemOptions, setItemOptions] = useState<string[]>([]);
    const [displayMessage, setDisplayMessage] = useState<string>("");
    const [displayPlayerHp, setDisplayPlayerHp] = useState<number>(0);
    const [displayEnemyHp, setDisplayEnemyHp] = useState<number>(0);
    const [isPlaying, setIsPlaying] = useState(false);
    const [isActing, setIsActing] = useState(false);
    const [shakeTarget, setShakeTarget] = useState<"player" | "enemy" | null>(null);
    const [shakeKey, setShakeKey] = useState(0);
    const [damageFloater, setDamageFloater] = useState<DamageFloater | null>(null);
    const [battleEffect, setBattleEffect] = useState<BattleEffect | null>(null);
    const [enemyDefeated, setEnemyDefeated] = useState(false);
    const { playBgm, playSfx } = useAudio();
    const router = useRouter();
    const effectKeyRef = useRef(0);
    useRequireSession();
    useRequireActiveGame();
    const nextEffectKey = () => ++effectKeyRef.current;

    useEffect(() => {
        playBgm(BGM.battle);
        return () => playBgm(BGM.explore);
    }, [playBgm]);

    useEffect(() => {
        const start = async () => {
            try {
                const response = await apiGet("/battle");
                setBattleState(response);
                setDisplayMessage(response.message);
                setDisplayPlayerHp(response.playerState.hp);
                setDisplayEnemyHp(response.enemyState.hp);
                setEnemyDefeated(false);
                setError(null);
            } catch (err: unknown) {
                if (err instanceof Error) {
                    setError(err.message);
                } else {
                    setError("通信に失敗しました。");
                }
            }
        };
        start();
    }, []);

    useEffect(() => {
        if (!shakeTarget) return;
        const id = setTimeout(() => setShakeTarget(null), 450);
        return () => clearTimeout(id);
    }, [shakeTarget, shakeKey]);

    useEffect(() => {
        if (!damageFloater) return;
        const id = setTimeout(() => setDamageFloater(null), 600);
        return () => clearTimeout(id);
    }, [damageFloater])

    useEffect(() => {
        if (!battleEffect) return;
        const id = setTimeout(() => setBattleEffect(null), 500);
        return () => clearTimeout(id);
    }, [battleEffect])


    const triggerShake = (target: "player" | "enemy") => {
        setShakeTarget(target);
        setShakeKey((key) => key + 1);
    };

    const triggerFlash = (type: FlashType, target: "player" | "enemy") => {
        setBattleEffect({ kind: "flash", type, target, key: nextEffectKey() });
    };

    const triggerSprite = (type: SpriteEffectType, target: "player" | "enemy") => {
        setBattleEffect({ kind: "sprite", type, target, key: nextEffectKey() });
    };

    const updateHpWithShake = (
        target: "player" | "enemy",
        nextHp: number,
        damage: number,
        message?: string,
    ) => {
        if (message?.includes("防御")) {
            triggerFlash("defend", target === "player" ? "enemy" : "player");
            playSfx(SFX.defend);
            return;
        }
        
        if (message?.includes("逃げた")) {
            playSfx(SFX.escape, 1, 1000);
            return;
        }

        if (damage > 0) {
            triggerShake(target);
            triggerDamageFloat(target, damage);
            triggerFlash("hit", target);
        }
        
        if (target === "player") {
            setDisplayPlayerHp(nextHp);
            playSfx(SFX.punch);
        } else {
            setDisplayEnemyHp(nextHp);
            triggerSprite("slash", "enemy");
            playSfx(SFX.slash);
        }
    };

    const triggerDamageFloat = (target: "player" | "enemy", value: number) => {
        if (value === 0) return;
        setDamageFloater({ target, value, key: Date.now() });
    };

    const playEnemyDefeatAnimation = async (enemyHp: number) => {
        if (enemyHp !== 0) return;
        setEnemyDefeated(true);
        await sleep(800);
    };

    const playTurn = async (choice?: BattleChoice, itemName?: string) => {
        setItemOptions([]);
        setIsActing(true);
        try {
            if (choice) {
                const response = await apiPost("/battle/action", { playerChoice: choice });
                setIsPlaying(true);
                
                const isPlayerFastResult = isPlayerFast(response.playerState.spd, response.enemyState.spd, choice, response.battleState.enemyChoice);
                const messages = messageDivision(response.message, isPlayerFastResult, response.playerState.name, response.enemyState.name);
                
                setBattleState(response);
                setDisplayMessage(messages[0]);

                if (isPlayerFastResult) {
                    updateHpWithShake("enemy", response.enemyState.hp, response.battleState.damageToEnemy, messages[0]);
                } else {
                    updateHpWithShake("player", response.playerState.hp, response.battleState.damageToPlayer, messages[0]);
                }

                await sleep(700);
                setDisplayMessage(response.message);

                if (isPlayerFastResult && messages.length > 1) {
                    updateHpWithShake("player", response.playerState.hp, response.battleState.damageToPlayer, messages[1]);
                } else if (messages.length > 1) {
                    updateHpWithShake("enemy", response.enemyState.hp, response.battleState.damageToEnemy, messages[1]);
                }

                await playEnemyDefeatAnimation(response.enemyState.hp);
            }
            if (itemName) {
                const response = await apiPost("/battle/action", {
                    playerChoice: BattleChoice.ITEM,
                    itemName,
                });
                setIsPlaying(true);
                setBattleState(response);
                const isPlayerFastResult = isPlayerFast(response.playerState.spd, response.enemyState.spd, BattleChoice.ITEM, response.battleState.enemyChoice);
                const messages = messageDivision(response.message, isPlayerFastResult, response.playerState.name, response.enemyState.name);
                setDisplayMessage(messages[0]);

                let currentHp = displayPlayerHp;
                currentHp = Math.min(
                    currentHp + (getItemHealAmount(itemName) ?? 0),
                    response.playerState.maxHp
                );

                setDisplayPlayerHp(currentHp);
                triggerFlash("heal", "player");
                playSfx(SFX.healBattle);
                await sleep(700);
                setDisplayMessage(response.message);
                updateHpWithShake("player", response.playerState.hp, response.battleState.damageToPlayer, messages[1]);
                currentHp = response.playerState.hp;

                await playEnemyDefeatAnimation(response.enemyState.hp);
            }

            setIsPlaying(false);
            setError(null);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("通信に失敗しました。");
            }
        } finally {
            setIsActing(false);
        }
    }

    const handleChoice = async (choice: BattleChoice) => {
        if (isActing || battleState?.battleState.finished) return;

        if (choice === BattleChoice.ITEM) {
            setItemOptions(Object.keys(battleState?.playerState.ownedItems ?? {}));
            return;
        }
        await playTurn(choice);
    };

    const handleItemChoice = async (itemName: string) => {
        if (isActing || battleState?.battleState.finished) return;

        setItemOptions([]);
        setIsActing(true);
        
        await playTurn(undefined, itemName);
    };

    const handleItemBack = () => {
        setItemOptions([]);
    };

    const finished = battleState?.battleState.finished === true;
    const battleResult =
        finished && battleState
            ? parseBattleResult(
                battleState.message,
                battleState.playerState.level,
                battleState.playerState.hp,
                battleState.enemyState.enemyType
            )
            : null;

    return (
        <div className="relative min-h-[100dvh] w-full overflow-hidden bg-neutral-900">
            <div
                className="pointer-events-none absolute inset-0 bg-cover bg-center bg-no-repeat"
                style={{ backgroundImage: `url('${BACKGROUNDS.explore}')` }}
                aria-hidden
            />

            <div className="relative z-10 flex min-h-[100dvh] flex-col">
                {!finished || isPlaying ? (
                    <>
                        <BattleMessageBox message={displayMessage} />

                        {error ? <ErrorAlert message={error} /> : null}

                        <div className="flex flex-1 items-center justify-center pb-4 pt-24 sm:pt-28">
                            <BattleEnemyDisplay 
                            enemy={battleState?.enemyState} 
                            hp={displayEnemyHp} 
                            isShaking={shakeTarget === "enemy"} 
                            shakeKey={shakeKey} 
                            damageFloater={damageFloater?.target === "enemy" ? damageFloater : null}
                            effect={battleEffect}
                            isDefeated={enemyDefeated}
                            />
                        </div>

                        <BattleCommandBox
                            player={battleState?.playerState}
                            hp={displayPlayerHp}
                            disabled={isActing || isPlaying || battleState === null}
                            onChoice={handleChoice}
                            itemOptions={itemOptions}
                            onItemChoice={handleItemChoice}
                            onItemBack={handleItemBack}
                            ownedItems={battleState?.playerState.ownedItems ?? {}}
                            isShaking={shakeTarget === "player"}
                            shakeKey={shakeTarget === "player" ? shakeKey : 0}
                            damageFloater={damageFloater?.target === "player" ? damageFloater : null}
                            effect={battleEffect}
                        />
                    </>
                ) : null}

                {finished && !isPlaying && battleResult ? (
                    <BattleResultModal
                        result={battleResult}
                        onBack={() => {
                            if (battleResult.backPath === "/gameover") {
                                markGameFinished();
                            }
                            router.push(battleResult.backPath);
                        }}
                    />
                ) : null}

                {finished && !isPlaying && !battleResult ? (
                    <div className="absolute inset-0 z-30 flex items-center justify-center px-4">
                        <p className="rounded-md border-2 border-black bg-white/90 px-4 py-2 font-bold text-neutral-900">
                            {battleState?.message || "戦闘終了"}
                        </p>
                    </div>
                ) : null}
            </div>
        </div>
    );
}
