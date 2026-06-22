import "./DrawerView.css";
import { useState, useCallback } from "react";
import { DrawingCanvas } from "./DrawingCanvas";
import { PlayersList } from "./PlayersList";
import { Panel } from "../common/Panel";
import { Button } from "../common/Button";
import { Logo } from "../common/Logo";
import { usePixelBatcher } from "../../hooks/usepixeldrawer";
import { DRAW_COLORS } from "../../types/game";
import type { Pixel, Room } from "../../types/game";

interface DrawerViewProps {
    room: Room;
    roomId: string;
}

export function DrawerView({ room, roomId }: DrawerViewProps) {
    const [selectedColor, setSelectedColor] = useState(0);
    const [localPixels, setLocalPixels] = useState<Pixel[]>(room.pixels ?? []);
    const { addPoint, flush } = usePixelBatcher(roomId, true);

    const handleAddPixel = useCallback((x: number, y: number, colorIndex: number) => {
        addPoint(x, y, colorIndex);
        setLocalPixels((prev) => [...prev, { x, y, colorIndex }]);
    }, [addPoint]);

    return (
        <div className="drawer-view">
            <div className="drawer-view__left">
                <Panel title="Налаштування гри" className="drawer-view__settings">
                    <div className="drawer-view__setting-row">
                        <span className="drawer-view__label">Час малювання</span>
                        <span className="drawer-view__value">{room.durationOfRound}с</span>
                    </div>
                    <div className="drawer-view__setting-row">
                        <span className="drawer-view__label">Раунди</span>
                        <span className="drawer-view__value">{room.currentRound ?? 1} / {room.numberOfRounds}</span>
                    </div>
                    <div className="drawer-view__setting-row">
                        <span className="drawer-view__label">Слово</span>
                        <span className="drawer-view__word">{room.wordToGuess ?? "—"}</span>
                    </div>
                </Panel>

                <PlayersList players={room.displayPlayers} />
            </div>

            <div className="drawer-view__center">
                <Logo size="medium" />
                <div className="drawer-view__timer">
                    {room.secondsLeft ?? 0}с
                </div>
                <DrawingCanvas
                    pixels={localPixels}
                    isDrawing={true}
                    selectedColor={selectedColor}
                    onAddPixel={handleAddPixel}
                />
                <Button className="drawer-view__submit" onClick={() => void flush()}>
                    Надіслати
                </Button>
            </div>

            <div className="drawer-view__right">
                <Panel title="Вибір кольору" className="drawer-view__colors">
                    <div className="drawer-view__color-grid">
                        {DRAW_COLORS.map((color, index) => (
                            <button
                                key={index}
                                className={`drawer-view__color-btn ${selectedColor === index ? "drawer-view__color-btn--selected" : ""}`}
                                style={{ backgroundColor: color }}
                                onClick={() => setSelectedColor(index)}
                                aria-label={`Колір ${index}`}
                            />
                        ))}
                    </div>
                </Panel>
            </div>
        </div>
    );
}
