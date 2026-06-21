import { useCallback, useState } from "react";
import { clearStoredToken, getStoredToken, setStoredToken } from "../api/client";
import type { AuthResponse } from "../types/api";

export interface CurrentUser {
    userId: number;
    username: string;
    avatarFileName: string;
}

const USER_STORAGE_KEY = "crocodile_user";

function readStoredUser(): CurrentUser | null {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw) as CurrentUser;
    } catch {
        return null;
    }
}

export function useAuth() {
    const [user, setUser] = useState<CurrentUser | null>(() => readStoredUser());
    const [token, setTokenState] = useState<string | null>(() => getStoredToken());

    const signIn = useCallback((auth: AuthResponse) => {
        const nextUser: CurrentUser = {
            userId: auth.userId,
            username: auth.username,
            avatarFileName: auth.avatarFileName,
        };
        setStoredToken(auth.token);
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(nextUser));
        setTokenState(auth.token);
        setUser(nextUser);
    }, []);

    const signOut = useCallback(() => {
        clearStoredToken();
        localStorage.removeItem(USER_STORAGE_KEY);
        setTokenState(null);
        setUser(null);
    }, []);

    return {
        user,
        token,
        isAuthenticated: token !== null,
        signIn,
        signOut,
    };
}
