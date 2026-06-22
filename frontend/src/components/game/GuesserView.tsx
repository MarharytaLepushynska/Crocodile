import "./GuesserView.css";
import { useState } from "react";
import { DrawingCanvas } from "./DrawingCanvas";
import { PlayersList } from "./PlayersList";
import { Panel } from "../common/Panel";
import { Button } from "../common/Button";
import { InputForm } from "../common/InputForm";
import { submitGuess } from "../../api/roomApi";
import type { Room } from "../../types/game";

interface GuesserViewProps {
    room: Room;
    roomId: string;
}

export function GuesserView({ room, roomId }: GuesserViewProps) {
    const [guess, setGuess] = useState("");
    const [feedback, setFeedback] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleGuess() {
        const trimmed = guess.trim();
        if (!trimmed) return;
        setIsSubmitting(true);
        setFeedback(null);
        try {
            const result = await submitGuess(Number(roomId), { guess: trimmed });
            if (result.correct) {
                setFeedback("Правильно!");
                setGuess("");
            } else {
                setFeedback("Невірно, спробуй ще!");
            }
        } catch {
            setFeedback("Помилка при відправці");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="guesser-view">
            <div className="guesser-view__left">
                <Panel title="Налаштування гри" className="guesser-view__settings">
                    <div className="guesser-view__setting-row">
                        <span className="guesser-view__label">Час малювання</span>
                        <span className="guesser-view__value">{room.durationOfRound}с</span>
                    </div>
                    <div className="guesser-view__setting-row">
                        <span className="guesser-view__label">Раунди</span>
                        <span className="guesser-view__value">{room.currentRound ?? 1} / {room.numberOfRounds}</span>
                    </div>
                </Panel>

                <PlayersList players={room.displayPlayers} />
            </div>

            <div className="guesser-view__center">
                <div className="guesser-view__timer">
                    {room.secondsLeft ?? 0}с
                </div>
                <DrawingCanvas
                    pixels={room.pixels ?? []}
                    isDrawing={false}
                    selectedColor={0}
                />
            </div>

            <div className="guesser-view__right">
                <Panel title="Твоя відповідь" className="guesser-view__guess-panel">
                    <InputForm
                        placeholder="Введи слово..."
                        value={guess}
                        onChange={(e) => setGuess(e.target.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") void handleGuess(); }}
                    />
                    {feedback && (
                        <p className={`guesser-view__feedback ${feedback === "Правильно!" ? "guesser-view__feedback--correct" : "guesser-view__feedback--wrong"}`}>
                            {feedback}
                        </p>
                    )}
                    <Button
                        className="guesser-view__submit"
                        onClick={() => void handleGuess()}
                        disabled={isSubmitting || guess.trim().length === 0}
                    >
                        Відповісти
                    </Button>
                </Panel>
            </div>
        </div>
    );
}
