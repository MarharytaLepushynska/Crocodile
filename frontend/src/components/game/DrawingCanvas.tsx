import "./DrawingCanvas.css";
import { useEffect, useRef } from "react";
import type { Pixel } from "../../types/game";
import { DRAW_COLORS } from "../../types/game";

const CANVAS_SIZE = 600;
const PIXEL_SIZE = 4;

interface DrawingCanvasProps {
    pixels: Pixel[];
    isDrawing: boolean;
    selectedColor: number;
    onAddPixel?: (x: number, y: number, colorIndex: number) => void;
}

export function DrawingCanvas({ pixels, isDrawing, selectedColor, onAddPixel }: DrawingCanvasProps) {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const isMouseDown = useRef(false);

    useEffect(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;

        ctx.fillStyle = "#ffffff";
        ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        for (const pixel of pixels) {
            ctx.fillStyle = DRAW_COLORS[pixel.colorIndex] ?? "#000000";
            ctx.fillRect(pixel.x * PIXEL_SIZE, pixel.y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
        }
    }, [pixels]);

    function getPixelCoords(e: React.MouseEvent<HTMLCanvasElement>): { x: number; y: number } {
        const canvas = canvasRef.current!;
        const rect = canvas.getBoundingClientRect();
        const scaleX = CANVAS_SIZE / rect.width;
        const scaleY = CANVAS_SIZE / rect.height;
        return {
            x: Math.floor((e.clientX - rect.left) * scaleX / PIXEL_SIZE),
            y: Math.floor((e.clientY - rect.top) * scaleY / PIXEL_SIZE),
        };
    }

    function handleDraw(e: React.MouseEvent<HTMLCanvasElement>) {
        if (!isDrawing || !isMouseDown.current || !onAddPixel) return;
        const { x, y } = getPixelCoords(e);
        onAddPixel(x, y, selectedColor);

        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;
        ctx.fillStyle = DRAW_COLORS[selectedColor] ?? "#000000";
        ctx.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
    }

    return (
        <canvas
            ref={canvasRef}
            width={CANVAS_SIZE}
            height={CANVAS_SIZE}
            className={`drawing-canvas ${isDrawing ? "drawing-canvas--active" : ""}`}
            onMouseDown={(e) => { isMouseDown.current = true; handleDraw(e); }}
            onMouseUp={() => { isMouseDown.current = false; }}
            onMouseLeave={() => { isMouseDown.current = false; }}
            onMouseMove={handleDraw}
        />
    );
}
