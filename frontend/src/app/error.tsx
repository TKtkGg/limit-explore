"use client";

import { useRouter } from "next/navigation";
import { Title } from "@/components/atoms/Title";
import { MainButton } from "@/components/atoms/MainButton";
import { BACKGROUNDS } from "@/lib/imagePaths";

type Props = {
    error: Error & { digest?: string };
    reset: () => void;
};

export default function ErrorPage({ error }: Props) {
    const router = useRouter();
    const message = error.message?.trim() || "エラーが発生しました";

    return (
        <div className="relative min-h-[100dvh] w-full overflow-hidden bg-neutral-900">
            <div
                className="pointer-events-none absolute inset-0 bg-cover bg-center bg-no-repeat"
                style={{ backgroundImage: `url('${BACKGROUNDS.woodPlank}')` }}
                aria-hidden
            />

            <div className="relative z-10 flex min-h-[100dvh] flex-col px-4 py-8 sm:px-8">
                <header className="pt-[4vh] text-center sm:pt-[6vh]">
                    <Title>ERROR</Title>
                </header>

                <main className="flex flex-1 flex-col items-center justify-center gap-8 sm:gap-10">
                    <p className="max-w-xl px-2 text-center text-2xl font-bold text-white text-outline sm:text-3xl md:text-4xl">
                        {message}
                    </p>
                    <MainButton
                        onClick={() => {
                            if (window.history.length > 1) {
                                router.back();
                            } else {
                                router.push("/");
                            }
                        }}
                        kind="back"
                    >
                        戻る
                    </MainButton>
                </main>
            </div>
        </div>
    );
}
