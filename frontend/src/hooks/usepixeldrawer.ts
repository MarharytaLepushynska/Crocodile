import { useCallback, useEffect, useRef } from "react";
import { submitPixels } from "../api/roomApi";
import type { Pixel } from "../types/game";

const DEFAULT_FLUSH_INTERVAL_MS = 3000;

export function usePixelBatcher(
    roomId: string | null,
    isActive: boolean,
    flushIntervalMs: number = DEFAULT_FLUSH_INTERVAL_MS,
) {
    const queueRef = useRef<Pixel[]>([]);

    const addPoint = useCallback((x: number, y: number, colorIndex: number) => {
        queueRef.current.push({ x, y, colorIndex });
    }, []);

    const flush = useCallback(async () => {
        if (!roomId || queueRef.current.length === 0) return;
        const pending = queueRef.current;
        queueRef.current = [];
        try {
            await submitPixels(Number(roomId), pending);
        } catch {
            queueRef.current = [...pending, ...queueRef.current];
        }
    }, [roomId]);

    useEffect(() => {
        if (!isActive || !roomId) return;

        const intervalId = window.setInterval(() => {
            void flush();
        }, flushIntervalMs);

        return () => {
            window.clearInterval(intervalId);
            void flush();
        };
    }, [isActive, roomId, flushIntervalMs, flush]);

    return { addPoint, flush };
}
