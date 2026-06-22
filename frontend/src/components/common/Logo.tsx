import "./Logo.css"

const LETTER_COLORS = [
    "var(--logo-color-1)",
    "var(--logo-color-2)",
    "var(--logo-color-3)",
    "var(--logo-color-4)",
    "var(--logo-color-1)",
    "var(--logo-color-5)",
    "var(--logo-color-6)",
    "var(--logo-color-7)",
    "var(--logo-color-3)",
];

const TEXT = "Crocodile";

interface LogoProps {
    size?: "large" | "medium";
}

export function Logo({ size = "large" }: LogoProps) {
    return (
        <h1 className={`logo logo--${size}`} aria-label={TEXT}>
            {TEXT.split("").map((letter, index) => (
                <span key={`${letter}-${index}`}
                      style={{ color: LETTER_COLORS[index % LETTER_COLORS.length] }}>
                    {letter}
                </span>
            ))}
        </h1>
    );
}
