import "./GamePage.css";
import { useParams } from "react-router-dom";
import { PageBackground } from "../components/layout/PageBackground";
import { Logo } from "../components/common/Logo";
import { DrawerView } from "../components/game/DrawerView";
import { GuesserView } from "../components/game/GuesserView";
import { ResultsView } from "../components/game/ResultsView";
import { WaitingPage } from "./WaitingPage";
import { useRoomPolling } from "../hooks/useroompolling";
import { useGameContext } from "../context/GameContext";

export default function GamePage() {
    const { roomId } = useParams<{ roomId: string }>();
    const { user } = useGameContext();
    const { room, isLoading } = useRoomPolling(roomId ?? null);

    if (!roomId) return null;

    if (isLoading || !room) {
        return (
            <PageBackground>
                <p style={{ color: "var(--color-text-secondary)", textAlign: "center" }}>
                    Завантаження...
                </p>
            </PageBackground>
        );
    }

    if (room.status === "WAITING") {
        return <WaitingPage roomId={Number(roomId)} />;
    }

    if (room.status === "FINISHED") {
        return (
            <PageBackground>
                <div className="game-page">
                    <Logo size="medium" />
                    <ResultsView players={room.displayPlayers} />
                </div>
            </PageBackground>
        );
    }

    const isDrawer = user?.userId === room.currentDrawerId;

    return (
        <PageBackground>
            <div className="game-page">
                <Logo size="medium" />
                {isDrawer
                    ? <DrawerView room={room} roomId={roomId} />
                    : <GuesserView room={room} roomId={roomId} />
                }
            </div>
        </PageBackground>
    );
}
