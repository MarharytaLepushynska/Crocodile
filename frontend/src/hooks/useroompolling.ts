import { useCallback, useEffect, useRef, useState } from "react";
import { fetchRoomState } from "../api/roomApi";
import type { Room } from "../types/game";

const DEFAULT_POLL_INTERVAL_MS = 1500;

interface UseRoomPollingResult {
    room: Room | null;
    isLoading: boolean;
    error: string | null;
    refetchNow: () => Promise<void>;
}

export function useRoomPolling(
    roomId: string | null,
    intervalMs: number = DEFAULT_POLL_INTERVAL_MS,
): UseRoomPollingResult {
    const [room, setRoom] = useState<Room | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const isMountedRef = useRef(true);

    const fetchOnce = useCallback(async () => {
        if (!roomId) return;
        try {
            const nextRoom = await fetchRoomState(Number(roomId));
            if (isMountedRef.current) {
                setRoom(nextRoom);
                setError(null);
            }
        } catch (err) {
            if (isMountedRef.current) {
                setError(
                    err instanceof Error ? err.message : "Не вдалося оновити стан гри",
                );
            }
        } finally {
            if (isMountedRef.current) {
                setIsLoading(false);
            }
        }
    }, [roomId]);

    useEffect(() => {
        isMountedRef.current = true;
        if (!roomId) {
            setRoom(null);
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        void fetchOnce();

        const intervalId = window.setInterval(() => {
            void fetchOnce();
        }, intervalMs);

        return () => {
            isMountedRef.current = false;
            window.clearInterval(intervalId);
        };
    }, [roomId, intervalMs, fetchOnce]);

    return { room, isLoading, error, refetchNow: fetchOnce };
}
