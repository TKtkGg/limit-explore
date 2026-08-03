"use client";

import { useEffect, useState } from "react";
import { apiGet } from "@/lib/apiClient";
import { useRouter } from "next/navigation";
import { StatusState } from "@/type/types";

export default function ProgressPage() {
    const [error, setError] = useState<string | null>(null);
    const [score, setScore] = useState<number>(0);
    const [playerState, setPlayerState] = useState<StatusState | null>(null);
    const router = useRouter();

    useEffect(() => {
        const start = async () => {
            try {
                const response = await apiGet("/progress");
                setScore(response.score);
                setPlayerState(response.playerState);
                if (response.cleared) {
                    router.push("/gameover");
                }
            } catch (err: unknown) {
                if (err instanceof Error) {
                    setError(err.message);
                } else {
                    setError("通信に失敗しました。");
                }
            }
        }
        start();
    }, []);
    return (
        <div>
            <h1>Progress</h1>
            <p>Score: {score}</p>
            <p>Name: {playerState?.name}</p>
            <p>Level: {playerState?.level}</p>
            <p>Max HP: {playerState?.maxHp}</p>
            <p>HP: {playerState?.hp}</p>
            <p>ATK: {playerState?.atk}</p>
            <p>DEF: {playerState?.def}</p>
            <p>SPD: {playerState?.spd}</p>
            <p>EXP: {playerState?.exp}</p>
            <p>Gold: {playerState?.gold}</p>
            <p>Equipment: {playerState?.equipment.name}</p>
            <p>Owned Equipment: {playerState?.ownedEquipmentList.map((equipment) => equipment.name).join(", ")}</p>
            <p>Owned Cards: {playerState?.ownedCards.map((card) => card.name).join(", ")}</p>
            <p>Owned Items: {Object.entries(playerState?.ownedItems || {}).map(([item, count]) => `${item}: ${count}`).join(", ")}</p>
            {error && <p>{error}</p>}
            <button onClick={() => {
                router.push("/explore");
            }}>Explore</button>
        </div>
    );
}