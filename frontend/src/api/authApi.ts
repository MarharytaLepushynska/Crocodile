import { apiRequest, setStoredToken } from "./client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/api";

export async function register(
    request: RegisterRequest,
): Promise<AuthResponse> {
    const response = await apiRequest<AuthResponse>("/register", {
        method: "POST",
        body: request,
        requiresAuth: false,
    });
    setStoredToken(response.token);
    return response;
}

export async function login(request: LoginRequest): Promise<AuthResponse> {
    const response = await apiRequest<AuthResponse>("/login", {
        method: "POST",
        body: request,
        requiresAuth: false,
    });
    setStoredToken(response.token);
    return response;
}
