import "./WaitingPage.css";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { PageBackground } from "../components/layout/PageBackground";
import { Logo } from "../components/common/Logo";
import { Panel } from "../components/common/Panel";
import { Button } from "../components/common/Button";
import { fetchRoomState, startGame } from "../api/roomApi";
import { useGameContext } from "../context/GameContext";
import { ApiError } from "../api/client";
import type { Room } from "../types/game";

interface WaitingPageProps {
    roomId: number;
}

export function WaitingPage({ roomId }: WaitingPageProps) {
    const navigate = useNavigate();
    const { user } = useGameContext();
    const [room, setRoom] = useState<Room | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [isStarting, setIsStarting] = useState(false);

    useEffect(() => {
        let cancelled = false;

        async function poll() {
            try {
                const data = await fetchRoomState(roomId);
                if (cancelled) return;

                if (data.status === "IN_PROGRESS") {
                    navigate(`/room/${roomId}`);
                    return;
                }

                setRoom(data);
            } catch {
                if (!cancelled) setError("Не вдалося отримати стан кімнати");
            }
        }

        void poll();
        const interval = setInterval(() => void poll(), 3000);
        return () => {
            cancelled = true;
            clearInterval(interval);
        };
    }, [roomId, navigate]);

    async function handleStart() {
        setIsStarting(true);
        setError(null);
        try {
            await startGame(roomId);
            navigate(`/room/${roomId}`, { replace: true });
        } catch (err) {
            setError(err instanceof ApiError ? err.message : "Не вдалося почати гру");
            setIsStarting(false);
        }
    }

    const isCreator = user != null && room != null && user.userId === room.creatorId;

    return (
        <PageBackground>
            <div className="waiting-page">
                <Logo size="medium" />
                <Panel title="Очікування гравців" className="waiting-page__panel">
                    {room && (
                        <>
                            <div className="waiting-page__invite">
                                <span className="waiting-page__invite-label">Код кімнати:</span>
                                <span className="waiting-page__invite-code">{room.inviteCode}</span>
                            </div>

                            <ul className="waiting-page__players">
                                {room.displayPlayers.map((player) => (
                                    <li key={player.userId} className="waiting-page__player">
                                        <img
                                            src={`/avatars/${player.avatarFileName}`}
                                            alt={player.username}
                                            className="waiting-page__avatar"
                                        />
                                        <span className="waiting-page__username">{player.username}</span>
                                        {player.isAdmin && (
                                            <span className="waiting-page__badge">creator</span>
                                        )}
                                    </li>
                                ))}
                            </ul>

                            {error && <p className="waiting-page__error">{error}</p>}

                            <Button
                                className="waiting-page__start-button"
                                onClick={() => void handleStart()}
                                disabled={!isCreator || isStarting}
                            >
                                {isCreator ? "Почати гру" : "Очікування організатора..."}
                            </Button>
                        </>
                    )}

                    {!room && !error && (
                        <p className="waiting-page__loading">Завантаження...</p>
                    )}

                    {error && !room && <p className="waiting-page__error">{error}</p>}
                </Panel>
            </div>
        </PageBackground>
    );
}
