import "./Button.css";
import type { ButtonHTMLAttributes, ReactNode } from "react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children: ReactNode;
    variant?: "primary" | "secondary";
}

export function Button({
    children,
    variant = "primary",
    type = "button",
    className,
    ...rest
}: ButtonProps) {
    const classNames = ["app-button", `app-button--${variant}`, className]
        .filter(Boolean)
        .join(" ");

    return (
        <button type={type} className={classNames} {...rest}>
            {children}
        </button>
    );
}
