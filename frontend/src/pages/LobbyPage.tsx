import "./LobbyPage.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { PageBackground } from "../components/layout/PageBackground";
import { Logo } from "../components/common/Logo";
import { JoinGamePanel } from "../components/lobby/JoinGamePanel";
import { CreateGamePanel } from "../components/lobby/CreateGamePanel";
import { createRoom, joinByInviteCode } from "../api/roomApi";
import { ApiError } from "../api/client";

export function LobbyPage() {
    const navigate = useNavigate();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleJoin(inviteCode: string) {
        setIsSubmitting(true);
        setError(null);
        try {
            const response = await joinByInviteCode(inviteCode);
            navigate(`/room/${response.roomId}`);
        } catch (err) {
            setError(
                err instanceof ApiError ? err.message : "Failed to join the room",
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    async function handleCreate(drawTimeSeconds: number, rounds: number) {
        setIsSubmitting(true);
        setError(null);
        try {
            const response = await createRoom({
                numberOfRounds: rounds,
                durationOfRound: drawTimeSeconds,
            });
            navigate(`/room/${response.roomId}`);
        } catch (err) {
            setError(
                err instanceof ApiError ? err.message : "Failed to create room",
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <PageBackground>
            <div className="lobby-page">
                <Logo/>
                {error && <p className="lobby-page__error">{error}</p>}
                <div className="lobby-page__panels">
                    <JoinGamePanel onJoin={(id) => void handleJoin(id)}
                                   isSubmitting={isSubmitting}/>
                    <CreateGamePanel onCreate={(drawTime, rounds) => void handleCreate(drawTime, rounds)}
                                     isSubmitting={isSubmitting}/>
                </div>
            </div>
        </PageBackground>
    );
}
