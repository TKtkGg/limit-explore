"use client";

import { useState, useEffect } from "react";
import { apiPost, apiGet } from "@/lib/apiClient";
import { useRouter } from "next/navigation";
import { MainButton } from "@/components/atoms/MainButton";
import { RouteAllowButton } from "@/components/molecules/RouteAllowButton";
import { Title } from "@/components/atoms/Title";
import { ErrorAlert } from "@/components/atoms/ErrorAlert";
import { useAudio } from "@/components/providers/AudioProvider";
import { SFX, BGM } from "@/lib/audioPaths";
import { BACKGROUNDS, ICONS } from "@/lib/imagePaths";
import { IconButton } from "@/components/atoms/IconButton";
import { SettingModal } from "@/components/molecules/SettingModal";
import { useRequireSession } from "@/hooks/useRequireSession";
import { useRequireActiveGame } from "@/hooks/useRequireActiveGame";
import { markGameFinished } from "@/lib/session";


/** 進行ボタン配置: 中央(上向き)・左下・右下 に routeOptions の 0,1,2 を対応 */
const ROUTE_SLOTS = [
    { positionClass: "left-1/2 top-[50%] sm:top-[36%] -translate-x-1/2 -translate-y-1/2", rotationClass: "rotate-0" },
    { positionClass: "left-[10%] bottom-[20%] -translate-x-1/2", rotationClass: "-rotate-90" },
    { positionClass: "right-[10%] bottom-[20%] translate-x-1/2", rotationClass: "rotate-90" }
] as const;

export default function ExplorePage() {
    const [remainingSteps, setRemainingSteps] = useState(25);
    const [currentLaps, setCurrentLaps] = useState(1);
    const [totalLaps, setTotalLaps] = useState(3);
    const [stopped, setStopped] = useState(false);
    const [error, setError] = useState<Error | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [routeOptions, setRouteOptions] = useState<string[]>([]);
    const [message, setMessage] = useState("");
    const [settingModalOpen, setSettingModalOpen] = useState(false);
    const router = useRouter();
    const { playBgm, playSfx } = useAudio();
    useRequireSession();
    useRequireActiveGame();

    useEffect(() => {
        playBgm(BGM.explore);
    }, [playBgm]);

    useEffect(() => {
        const start = async () => {
            try {
                const response = await apiGet("/move/status");
                setRemainingSteps(response.remainingSteps);
                setCurrentLaps(response.currentLaps);
                setTotalLaps(response.totalLaps);
                setStopped(response.stopped);
                setRouteOptions(response.routeOptions);
                setMessage(response.message);
                setError(null);
                if(response.stopped) {
                    markGameFinished();
                    router.push("/gameover");
                }
            } catch (error: unknown) {
                if (error instanceof Error) {
                setError(error);
                } else {
                setError(new Error("通信に失敗しました。"));
                }
            }
        };
        start();
    }, [router]);

    const handleMove = async (routeType: string) => {
        if (isLoading) return;
        setIsLoading(true);

        try {
            let response;
            if (routeType === "REST") {
                response = await apiPost("/move/rest", { routeType: routeType });
            } else if (routeType === "TREASURE") {
                response = await apiPost("/move", { routeType: routeType });
                router.push("/treasure");
            } else if (routeType === "CARD") {
                response = await apiPost("/move", { routeType: routeType });
                router.push("/card");
            } else if (routeType === "SHOP") {
                response = await apiPost("/move", { routeType: routeType });
                router.push("/shop");
            } else if (routeType === "BATTLE") {
                response = await apiPost("/move", { routeType: routeType });
                router.push("/battle");
            } else {
                response = await apiPost("/move", { routeType: routeType });
            }
            setRemainingSteps(response.remainingSteps);
            setCurrentLaps(response.currentLaps);
            setTotalLaps(response.totalLaps);
            setStopped(response.stopped);
            setRouteOptions(response.routeOptions);
            setMessage(response.message);
            setError(null);
            if(routeType === "REST") {
                playSfx(SFX.rest);
                if (response.stopped) {
                    markGameFinished();
                    router.push("/gameover");
                    return;
                }
            } else {
                playSfx(SFX.go);
            }
        } catch (error: unknown) {
            if (error instanceof Error) {
                setError(error);
            } else {
                setError(new Error("通信に失敗しました。"));
            }
        } finally {
            setIsLoading(false);
        }
    };

    const disabled = isLoading || stopped;

    return (
        <div className="relative min-h-[100dvh] w-full overflow-hidden bg-neutral-900">
            {/* 背景 */}
            <div
                className="pointer-events-none absolute inset-0 bg-cover bg-center bg-no-repeat"
                style={{ backgroundImage: `url('${BACKGROUNDS.explore}')` }}
                aria-hidden
            />

            {/* コンテンツ */}
            <div className="relative z-10 flex min-h-[100dvh] flex-col">
                {/* 上段: RESTART + 残りマス */}
                <div className="flex items-start justify-between px-4 pt-3 sm:px-6">
                    <IconButton 
                        onClick={() => setSettingModalOpen(true)}
                        img={ICONS.setting}
                    />
                    <div className="rounded-md border-2 border-black bg-white/90 px-3 py-1.5 text-xs font-bold tabular-nums text-neutral-900 shadow-[2px_2px_0_#000] sm:text-sm">
                        ラップ: {currentLaps} / {totalLaps} 残り {remainingSteps} マス
                    </div>
                </div>

                {/* タイトル */}
                <header className="px-4 pt-2 text-center sm:pt-4">
                    <Title>EXPLORE</Title>
                    {message ? (
                        <p
                        className={`mx-auto mt-2 max-w-md rounded-md border-2 border-black bg-black/55 px-3 py-2 text-sm font-bold text-white text-outline sm:text-base`}
                        >
                        {message}
                        </p>
                    ) : null}
                </header>

                {/* エラー */}
                {error ? (
                    <ErrorAlert message={error.message} />
                ) : null}

                {stopped ? (
                    <p className="mx-4 mt-2 text-center text-sm font-bold text-amber-200 [text-shadow:0_1px_2px_#000]">
                        マスが尽きました。まもなくゲームオーバーへ…
                    </p>
                ) : null}

                {/* 矢印エリア */}
                <div className="relative flex-1 min-h-[280px] sm:min-h-[360px]">
                    {ROUTE_SLOTS.map((slot, index) => {
                        const option = routeOptions[index];
                        if (!option) return null;
                        return (
                            <RouteAllowButton
                                key={`${option}-${index}`}
                                option={option}
                                slot={slot}
                                handleMove={handleMove}
                                disabled={disabled}
                            />
                        );
                    })}
                </div>

                {/* ローディング */}
                {isLoading ? (
                    <p className="pb-2 text-center text-xs font-bold text-white [text-shadow:0_1px_2px_#000]">
                        送信中…
                    </p>
                ) : null}

                {/* STATUS（下部固定風） */}
                <footer className="mt-auto flex justify-center px-4 pb-6 pt-4 sm:pb-8">
                    <MainButton onClick={() => router.push("/status")}>STATUS</MainButton>
                </footer>
            </div>
            {settingModalOpen ? (
                <SettingModal
                    onClose={() => setSettingModalOpen(false)}
                />
            ) : null}
        </div>
    );
}
