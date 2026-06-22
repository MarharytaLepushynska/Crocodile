import { useState } from "react";
import { Panel } from "../common/Panel";
import { InputForm } from "../common/InputForm";
import { Button } from "../common/Button";

interface JoinGamePanelProps {
    onJoin: (roomId: string) => void;
    isSubmitting?: boolean;
}

export function JoinGamePanel({onJoin, isSubmitting}: JoinGamePanelProps) {
    const [roomId, setRoomId] = useState("");

    function handleSubmit() {
        const trimmed = roomId.trim();
        if (trimmed.length === 0) return;
        onJoin(trimmed);
    }

    return (
        <Panel title="Приєднатись до гри">
            <label className="field-label" htmlFor="game-id-input">
                Введіть код кімнати
            </label>
            <InputForm
                id = "game-id-input"
                value ={roomId}
                onChange={(event) => setRoomId(event.target.value)}
                placeholder=""
                />
            <div className="panel__actions">
                <Button onClick={handleSubmit} disabled={isSubmitting || roomId.trim().length === 0}>
                    Приєднатись
                </Button>
            </div>
        </Panel>
    );
}
