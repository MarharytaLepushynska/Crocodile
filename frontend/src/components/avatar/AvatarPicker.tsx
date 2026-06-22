import "./AvatarPicker.css";
import { useState } from "react";
import { AVAILABLE_AVATARS } from "../../types/game";
import type { Avatar } from "../../types/game";

interface AvatarPickerProps {
    onChange?: (avatar: Avatar) => void;
}

export function AvatarPicker({ onChange }: AvatarPickerProps) {
    const [index, setIndex] = useState(0);

    function goTo(nextIndex: number) {
        const wrapped =
            (nextIndex + AVAILABLE_AVATARS.length) % AVAILABLE_AVATARS.length;
        setIndex(wrapped);
        onChange?.(AVAILABLE_AVATARS[wrapped]);
    }

    return (
        <div className="avatar-picker">
            <button
                type="button"
                className="avatar-picker__arrow"
                aria-label="Попередній аватар"
                onClick={() => goTo(index - 1)}
            >
                &lt;
            </button>

            <img
                className="avatar-picker__preview"
                src={`/avatars/${AVAILABLE_AVATARS[index].fileName}`}
                alt={`Аватар ${index + 1}`}
            />

            <button
                type="button"
                className="avatar-picker__arrow"
                aria-label="Наступний аватар"
                onClick={() => goTo(index + 1)}
            >
                &gt;
            </button>
        </div>
    );
}
