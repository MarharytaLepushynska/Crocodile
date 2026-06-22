import "./PageBackground.css";
import type { ReactNode } from "react";

interface PageBackgroundProps {
    children: ReactNode;
    align?: "center" | "start";
}

export function PageBackground({ children, align = "center" }: PageBackgroundProps) {
    return (
        <div className={`page-background page-background--${align}`}>
            {children}
        </div>
    );
}
