import { createContext, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useAuth } from "../hooks/useAuth.ts";
import type { CurrentUser } from "../hooks/useAuth.ts";
import type { AuthResponse } from "../types/api";

interface GameContextValue {
    user: CurrentUser | null;
    isAuthenticated: boolean;
    signIn: (auth: AuthResponse) => void;
    signOut: () => void;
    activeRoomId: string | null;
    setActiveRoomId: (roomId: string | null) => void;
}

const GameContext = createContext<GameContextValue | undefined>(undefined);

export function GameProvider({ children }: { children: ReactNode }) {
    const { user, isAuthenticated, signIn, signOut } = useAuth();
    const [activeRoomId, setActiveRoomId] = useState<string | null>(null);

    const value = useMemo<GameContextValue>(
        () => ({
            user,
            isAuthenticated,
            signIn,
            signOut,
            activeRoomId,
            setActiveRoomId,
        }),
        [user, isAuthenticated, signIn, signOut, activeRoomId],
    );

    return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
}

export function useGameContext(): GameContextValue {
    const context = useContext(GameContext);
    if (!context) {
        throw new Error("useGameContext must be inside GameProvider");
    }
    return context;
}
