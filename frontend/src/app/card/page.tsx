"use client";

import { useEffect, useState } from "react";
import { apiPost, apiGet } from "@/lib/apiClient";
import { useRouter } from "next/navigation";
import { CardState } from "@/type/types";
import { Title } from "@/components/atoms/Title";
import { MainButton } from "@/components/atoms/MainButton";
import { ErrorAlert } from "@/components/atoms/ErrorAlert";
import { CardButton } from "@/components/molecules/CardButton";
import { useAudio } from "@/components/providers/AudioProvider";
import { BGM } from "@/lib/audioPaths";
import { BACKGROUNDS } from "@/lib/imagePaths";
import { useRequireSession } from "@/hooks/useRequireSession";
import { useRequireActiveGame } from "@/hooks/useRequireActiveGame";

export default function CardPage() {
    const router = useRouter();
    const [cards, setCards] = useState<CardState[] | null>(null);
    const [chosenCard, setChosenCard] = useState<CardState | null>(null);
    const [isDefeatedBoss, setIsDefeatedBoss] = useState(false);
    const [backPath, setBackPath] = useState<string | null>(null);
    const [error, setError] = useState<Error | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isChoosing, setIsChoosing] = useState(false);
    const { playBgm } = useAudio();
    useRequireSession();
    useRequireActiveGame();
    
    useEffect(() => {
        playBgm(BGM.cave);
        return () => playBgm(BGM.explore);
    }, [playBgm]);

    useEffect(() => {
        const fetchCards = async () => {
            setIsLoading(true);
            try {
                const response = await apiGet("/card");
                setCards(response.display ?? []);
                setChosenCard(null);
                setIsDefeatedBoss(response.defeatedBoss ?? false);
                console.log(response.defeatedBoss);
                setBackPath((response.defeatedBoss ?? false) ? "/progress" : "/explore");
                setError(null);
            } catch (err: unknown) {
                if (err instanceof Error) {
                    setError(err);
                } else {
                    setError(new Error("通信に失敗しました。"));
                }
            } finally {
                setIsLoading(false);
            }
        };
        fetchCards();
    }, []);

    const handleChooseCard = async (card: CardState) => {
        if (chosenCard || isChoosing || isLoading) return;

        setIsChoosing(true);
        try {
            const response = await apiPost("/card/choose", { chosenCard: card });
            setCards(response.display ?? []);
            setChosenCard(response.chosenCard ?? card);
            setError(null);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err);
            } else {
                setError(new Error("通信に失敗しました。"));
            }
        } finally {
            setIsChoosing(false);
        }
    };

    const titleText = (() => {
        if (isLoading) return "読み込み中…";
        if (chosenCard) return `"${chosenCard.name}"を手に入れた`;
        return "カードを選べ";
    })();

    const showBackFooter = chosenCard !== null || error !== null;

    return (
        <div className="relative min-h-[100dvh] w-full overflow-hidden bg-neutral-900">
            <div
                className="pointer-events-none absolute inset-0 bg-cover bg-center bg-no-repeat"
                style={{ backgroundImage: `url('${BACKGROUNDS.cave}')` }}
                aria-hidden
            />

            <div className="relative z-10 flex min-h-[100dvh] flex-col">
                <header className="px-4 pt-8 text-center sm:pt-10">
                    <Title>{titleText}</Title>
                </header>

                {error ? <ErrorAlert message={error.message} /> : null}

                <div className="flex flex-1 flex-wrap items-center justify-center px-4 py-8">
                    {cards?.map((card, index) => {
                        const isThisChosen =
                            chosenCard !== null && chosenCard.name === card.name;
                        const isDisabled =
                            isChoosing ||
                            isLoading ||
                            (chosenCard !== null && !isThisChosen);

                        return (
                            <CardButton 
                                key={`${card.name}-${index}`} 
                                card={card}
                                index={index} 
                                isDisabled={isDisabled}
                                isThisChosen={isThisChosen}
                                handleChooseCard={handleChooseCard}
                                isDefeatedBoss={isDefeatedBoss}
                            />
                        );
                    })}
                </div>

                {isChoosing ? (
                    <p className="pb-2 text-center text-xs font-bold text-white text-outline">
                        送信中…
                    </p>
                ) : null}

                {showBackFooter ? (
                    <footer className="absolute bottom-6 left-1/2 z-20 -translate-x-1/2 sm:bottom-8 sm:left-6 sm:translate-x-0">
                        <MainButton onClick={() => router.push(backPath ?? "/explore")} kind="back">戻る</MainButton>
                    </footer>
                ) : null}
            </div>
        </div>
    );
}
