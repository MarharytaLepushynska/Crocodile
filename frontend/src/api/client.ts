const API_BASE_URL: string =
    (import.meta.env.VITE_API_URL as string | undefined) ??
    "http://localhost:8080";

const TOKEN_STORAGE_KEY = "crocodile_token";

export class ApiError extends Error {
    status: number;

    constructor(message: string, status: number) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

export function getStoredToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setStoredToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearStoredToken(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
}

interface RequestOptions {
    method?: "GET" | "POST" | "PUT" | "DELETE";
    body?: unknown;
    requiresAuth?: boolean;
}

export async function apiRequest<TResponse>(
    path: string,
    options: RequestOptions = {},
): Promise<TResponse> {
    const { method = "GET", body, requiresAuth = true } = options;

    const headers: Record<string, string> = {
        "Content-Type": "application/json",
    };

    if (requiresAuth) {
        const token = getStoredToken();
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }
    }

    let response: Response;
    try {
        response = await fetch(`${API_BASE_URL}${path}`, {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch (networkError) {
        throw new ApiError(
            "Не вдалося звʼязатись з сервером. Перевірте підключення.",
            0,
        );
    }

    if (response.status === 204) {
        return undefined as TResponse;
    }

    const text = await response.text();
    const data = text ? JSON.parse(text) : undefined;

    if (!response.ok) {
        const message =
            (data && typeof data === "object" && "message" in data
                ? String((data as { message: unknown }).message)
                : undefined) ?? `Помилка запиту (${response.status})`;
        throw new ApiError(message, response.status);
    }

    return data as TResponse;
}
