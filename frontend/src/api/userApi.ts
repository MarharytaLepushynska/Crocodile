import { apiRequest } from "./client";
import type { UpdateAvatarRequest, UserResponse } from "../types/api";

export async function fetchUserProfile(userId: number): Promise<UserResponse> {
    return apiRequest<UserResponse>(`/user/${userId}`);
}

export async function updateMyAvatar(
    request: UpdateAvatarRequest,
): Promise<UserResponse> {
    return apiRequest<UserResponse>("/user", {
        method: "PUT",
        body: request,
    });
}
