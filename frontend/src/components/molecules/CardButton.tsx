import Image from "next/image";
import { CardState } from "@/type/types";
import { useAudio } from "@/components/providers/AudioProvider";
import { SFX } from "@/lib/audioPaths";
import { IMAGES } from "@/lib/imagePaths";

type Props = {
    card: CardState;
    index: number;
    isDisabled: boolean;
    isThisChosen: boolean;
    handleChooseCard: (card: CardState) => void;
    isDefeatedBoss: boolean;
}

export const CardButton = (props: Props) => {
    const { card, index, isDisabled, isThisChosen, handleChooseCard, isDefeatedBoss } = props;
    const { playSfx } = useAudio();
    return (
        <button
            type="button"
            onClick={() => {
                playSfx(SFX.card);
                handleChooseCard(card);
            }}
            disabled={isDisabled}
            aria-label={`カード ${index + 1}`}
            className="relative shrink-0 outline-none transition cursor-pointer focus-visible:ring-2 focus-visible:ring-sky-400 focus-visible:ring-offset-2 focus-visible:ring-offset-neutral-900 disabled:cursor-not-allowed disabled:opacity-40 disabled:grayscale"
        >
            {isThisChosen ? (
                <span className="relative block w-[180px] sm:w-[min(22vw,280px)] md:w-[200px]">
                    <Image
                        src={IMAGES.cardFront}
                        alt=""
                        width={200}
                        height={280}
                        className="h-auto w-full object-contain drop-shadow-[0_8px_24px_rgba(0,0,0,0.45)]"
                        unoptimized
                        priority
                    />
                    <div className="absolute inset-[9%_10%_11%_10%] flex flex-col overflow-hidden text-left text-neutral-900">
                        <p className="line-clamp-2 text-xs font-black leading-tight sm:text-sm">
                            {card.name}
                        </p>
                        <div className="min-h-0 flex-1 overflow-y-auto text-[10px] leading-snug sm:text-xs">
                            <p className="whitespace-pre-wrap break-words pr-0.5">
                                {card.text}
                            </p>
                        </div>
                    </div>
                </span>
            ) : (
                <span className="block transition enabled:active:translate-y-0.5 enabled:active:scale-[0.98]">
                    <Image
                        src={isDefeatedBoss ? IMAGES.specialCardBack : IMAGES.cardBack}
                        alt=""
                        width={200}
                        height={280}
                        className="h-auto w-[300px] object-contain drop-shadow-[0_8px_24px_rgba(0,0,0,0.45)] sm:w-[350px] md:w-[400px]"
                        unoptimized
                        priority={index === 0}
                    />
                </span>
            )}
        </button>
    )
}