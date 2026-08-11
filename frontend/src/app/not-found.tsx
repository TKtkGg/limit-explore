"use client";

import { useRouter } from "next/navigation";
import { Title } from "@/components/atoms/Title";
import { MainButton } from "@/components/atoms/MainButton";
import { BACKGROUNDS } from "@/lib/imagePaths";

export default function NotFound() {
    const router = useRouter();

    return (
        <div className="relative min-h-[100dvh] w-full overflow-hidden bg-neutral-900">
            <div
                className="pointer-events-none absolute inset-0 bg-cover bg-center bg-no-repeat"
                style={{ backgroundImage: `url('${BACKGROUNDS.woodPlank}')` }}
                aria-hidden
            />

            <div className="relative z-10 flex min-h-[100dvh] flex-col px-4 py-8 sm:px-8">
                <header className="pt-[4vh] text-center sm:pt-[6vh]">
                    <Title>404 NOT FOUND</Title>
                </header>

                <main className="flex flex-1 items-center justify-center">
                    <p className="text-center text-2xl font-bold text-white text-outline sm:text-3xl md:text-4xl">
                        存在しないURLです
                    </p>
                </main>

                <footer className="flex justify-center pb-2 sm:justify-start sm:pb-4">
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
                </footer>
            </div>
        </div>
    );
}
