"use client";

import { useEffect, useState } from "react";
import { apiGet } from "@/lib/apiClient";
import { useRouter } from "next/navigation";

export default function ProgressPage() {
    const [error, setError] = useState<string | null>(null);
    const [score, setScore] = useState<number>(0);
    const router = useRouter();

    useEffect(() => {
        const start = async () => {
            try {
                const response = await apiGet("/progress");
                setScore(response.score);
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
            {error && <p>{error}</p>}
            <button onClick={() => {
                router.push("/explore");
            }}>Explore</button>
        </div>
    );
}