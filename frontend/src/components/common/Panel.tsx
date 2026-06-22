import "./Panel.css";
import type { ReactNode } from "react";

interface PanelProps {
    title?: string;
    children: ReactNode;
    className?: string;
}

export function Panel({ title, children, className }: PanelProps) {
    const classNames = ["panel", className].filter(Boolean).join(" ");
    return (
        <div className={classNames}>
            {title && <h2 className="panel__title">{title}</h2>}
            {children}
        </div>
    );
}
