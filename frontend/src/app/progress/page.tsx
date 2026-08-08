"use client";

import { useEffect, useState } from "react";
import { apiGet, apiPost } from "@/lib/apiClient";
import { useRouter } from "next/navigation";
import { StatusState } from "@/type/types";
import { Title } from "@/components/atoms/Title";
import { MainButton } from "@/components/atoms/MainButton";
import { ErrorAlert } from "@/components/atoms/ErrorAlert";
import { BACKGROUNDS } from "@/lib/imagePaths";

function joinNames(names: string[]): string {
    return names.length > 0 ? names.join("・") : "なし";
}

export default function ProgressPage() {
    const [error, setError] = useState<string | null>(null);
    const [score, setScore] = useState<number>(0);
    const [cleared, setCleared] = useState(false);
    const [playerState, setPlayerState] = useState<StatusState | null>(null);
    const [isContinuing, setIsContinuing] = useState(false);
    const router = useRouter();

    useEffect(() => {
        const start = async () => {
            try {
                const response = await apiGet("/progress");
                setScore(response.score);
                setCleared(Boolean(response.cleared));
                setPlayerState(response.playerState);
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

    const handleBack = async () => {
        if (isContinuing) return;

        if (cleared) {
            router.push("/gameover");
            return;
        }

        setIsContinuing(true);
        try {
            await apiPost("/progress/continue");
            router.push("/explore");
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("通信に失敗しました。");
            }
        } finally {
            setIsContinuing(false);
        }
    };

    const equipmentText = joinNames(
        (playerState?.ownedEquipmentList ?? []).map((equipment) => equipment.name)
    );
    const cardText = joinNames(
        (playerState?.ownedCards ?? []).map((card) => card.name)
    );
    const itemText = joinNames(
        Object.entries(playerState?.ownedItems ?? {})
            .filter(([, count]) => count > 0)
            .map(([name, count]) => `${name}×${count}`)
    );

    return (
        <div className="relative h-[100dvh] w-full overflow-hidden bg-neutral-900">
            <div
                className="pointer-events-none absolute inset-0 bg-cover bg-center bg-no-repeat"
                style={{ backgroundImage: `url('${BACKGROUNDS.woodPlank}')` }}
                aria-hidden
            />

            <div className="relative z-10 flex h-full flex-col px-4 py-4 sm:px-8 sm:py-6">
                <header className="shrink-0 pt-2 text-center sm:pt-4">
                    <Title>PROGRESS</Title>
                </header>

                {error ? <ErrorAlert message={error} /> : null}

                <main className="flex min-h-0 flex-1 flex-col items-center py-4 sm:py-6">
                    <div className="flex h-full min-h-0 w-full max-w-[1130px] flex-col overflow-hidden border-[6px] border-[#8b6914] bg-black/85 text-white">
                        <div className="min-h-0 flex-1 overflow-y-auto px-6 py-6 sm:px-12 sm:py-8">
                            <div className="flex flex-col gap-8 lg:flex-row lg:items-start lg:justify-between lg:gap-10">
                                <div className="min-w-0 flex-1 space-y-1 font-black text-outline">
                                    <p className="text-3xl sm:text-4xl md:text-5xl">
                                        Name : {playerState?.name ?? "-"}
                                    </p>
                                    <p className="text-xl sm:text-2xl md:text-[32px]">
                                        Level : {playerState?.level ?? "-"}
                                    </p>
                                    <p className="text-xl sm:text-2xl md:text-[32px]">
                                        HP : {playerState?.hp ?? "-"}
                                    </p>
                                    <p className="text-xl sm:text-2xl md:text-[32px]">
                                        ATK : {playerState?.totalAtk ?? playerState?.atk ?? "-"}
                                    </p>
                                    <p className="text-xl sm:text-2xl md:text-[32px]">
                                        DEF : {playerState?.def ?? "-"}
                                    </p>
                                    <p className="text-xl sm:text-2xl md:text-[32px]">
                                        SPD : {playerState?.spd ?? "-"}
                                    </p>

                                    <div className="h-3 sm:h-4" />

                                    <p className="text-xl sm:text-2xl md:text-[32px]">
                                        EXP : {playerState?.exp ?? "-"} / {playerState?.nextLevelExp ?? "-"}
                                    </p>
                                    <p className="text-xl sm:text-2xl md:text-[32px]">
                                        GOLD : {playerState?.gold ?? "-"}
                                    </p>
                                </div>

                                <p className="absolute right-60 top-60 shrink-0 text-center font-black text-outline text-4xl sm:text-5xl md:text-6xl lg:text-right lg:text-7xl xl:text-[96px] xl:leading-none">
                                    SCORE : {score}
                                </p>
                            </div>

                            <div className="mt-8 space-y-1 font-black text-outline text-lg sm:mt-10 sm:text-2xl md:text-[32px]">
                                <p className="break-words">EQUIPMENT : {equipmentText}</p>
                                <p className="break-words">CARD：{cardText}</p>
                                <p className="break-words">ITEM : {itemText}</p>
                            </div>
                        </div>
                    </div>
                </main>

                <footer className="flex shrink-0 justify-center pb-1 sm:justify-start sm:pb-2">
                    <MainButton onClick={handleBack} kind="back" disabled={isContinuing}>
                        戻る
                    </MainButton>
                </footer>
            </div>
        </div>
    );
}
