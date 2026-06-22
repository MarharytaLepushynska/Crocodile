import { useState } from "react";
import { Panel } from "../common/Panel";
import { Button } from "../common/Button";

const DRAW_TIME_OPTIONS = [30, 60, 90, 120];
const ROUNDS_OPTIONS = [1, 2, 3, 5, 10];

interface CreateGamePanelProps {
    onCreate: (drawTimeSeconds: number, rounds: number) => void;
    isSubmitting?: boolean;
}

export function CreateGamePanel({ onCreate, isSubmitting }: CreateGamePanelProps) {
    const [drawTime, setDrawTime] = useState(DRAW_TIME_OPTIONS[1]);
    const [rounds, setRounds] = useState(ROUNDS_OPTIONS[1]);

    return(
        <Panel title="Create your game">
            <div className="panel__row">
                <div className="panel__field">
                    <label className="field-label" htmlFor="draw-time-select">
                        Draw time
                    </label>
                    <select
                        id="draw-time-select"
                        className="form-input"
                        value={drawTime}
                        onChange={(event) => setDrawTime(Number(event.target.value))}
                        >
                        {DRAW_TIME_OPTIONS.map((seconds) => (
                            <option key={seconds} value={seconds}>
                                {seconds}s
                            </option>
                        ))}
                    </select>
                </div>

                <div className="panel__field">
                    <label className="field-label" htmlFor="rounds-select">
                        Rounds
                    </label>
                    <select
                        id="rounds-select"
                        className="form-input"
                        value={rounds}
                        onChange={(event) => setRounds(Number(event.target.value))}
                        >
                        {ROUNDS_OPTIONS.map((count) => (
                            <option key={count} value={count}>
                                {count}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            <div className="panel__actions">
                <Button
                    onClick={() => onCreate(drawTime, rounds)}
                    disabled={isSubmitting}
                >
                    Play
                </Button>
            </div>
        </Panel>
    );
}
