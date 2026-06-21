import { apiRequest } from "./client";
import { fetchUserProfile } from "./userApi";
import type {
    CreateRoomRequest,
    CreateRoomResponse,
    GuessRequest,
    GuessResponse,
    JoinByInviteCodeResponse,
    ResultsResponse,
    RoomStateResponse,
    SubmitPixelsRequest,
} from "../types/api";
import type { DisplayPlayer, Room } from "../types/game";

export async function createRoom(
    request: CreateRoomRequest,
): Promise<CreateRoomResponse> {
    return apiRequest<CreateRoomResponse>("/rooms", {
        method: "POST",
        body: request,
    });
}

export async function joinRoomById(roomId: number): Promise<void> {
    await apiRequest<{ status: string }>(`/rooms/${roomId}/join`, {
        method: "POST",
    });
}

export async function joinByInviteCode(
    inviteCode: string,
): Promise<JoinByInviteCodeResponse> {
    return apiRequest<JoinByInviteCodeResponse>(`/join/${inviteCode}`, {
        method: "POST",
    });
}

export async function startGame(roomId: number): Promise<void> {
    await apiRequest<{ status: string }>(`/rooms/${roomId}/start`, {
        method: "POST",
    });
}

export async function finishGame(roomId: number): Promise<void> {
    await apiRequest<{ status: string }>(`/rooms/${roomId}/finish`, {
        method: "POST",
    });
}

export async function getResults(roomId: number): Promise<ResultsResponse> {
    return apiRequest<ResultsResponse>(`/rooms/${roomId}/results`);
}

export async function submitPixels(
    roomId: number,
    points: SubmitPixelsRequest["points"],
): Promise<void> {
    await apiRequest<{ status: string }>(`/rooms/${roomId}/pixels`, {
        method: "PUT",
        body: { points },
    });
}

export async function submitGuess(
    roomId: number,
    request: GuessRequest,
): Promise<GuessResponse> {
    return apiRequest<GuessResponse>(`/guess/${roomId}`, {
        method: "POST",
        body: request,
    });
}

export async function fetchRoomState(roomId: number): Promise<Room> {
    const state = await apiRequest<RoomStateResponse>(`/rooms/${roomId}`);

    const profiles = await Promise.all(
        state.players.map((player) => fetchUserProfile(player.userId)),
    );

    const displayPlayers: DisplayPlayer[] = state.players.map((player, index) => {
        const profile = profiles[index];
        return {
            userId: player.userId,
            score: player.score,
            username: profile.username,
            avatarFileName: profile.avatarFileName,
            isAdmin: player.userId === state.creatorId,
            isDrawer: player.userId === state.currentDrawerId,
        };
    });

    return {
        id: state.id,
        status: state.status,
        numberOfRounds: state.numberOfRounds,
        durationOfRound: state.durationOfRound,
        inviteCode: state.inviteCode,
        creatorId: state.creatorId,
        players: state.players,
        displayPlayers,
        currentRound: state.currentRound,
        currentDrawerId: state.currentDrawerId,
        secondsLeft: state.secondsLeft,
        wordToGuess: state.wordToGuess,
        pixels: state.pixels,
    };
}
