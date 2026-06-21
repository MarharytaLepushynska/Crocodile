import type { Pixel, RoomStatus } from "./game";

export interface RegisterRequest {
    username: string;
    password: string;
    avatarFileName: string;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    userId: number;
    username: string;
    avatarFileName: string;
}

export interface UserResponse {
    userId: number;
    username: string;
    avatarFileName: string;
}

export interface UpdateAvatarRequest {
    avatarFileName: string;
}

export interface CreateRoomRequest {
    numberOfRounds: number;
    durationOfRound: number;
}

export interface CreateRoomResponse {
    roomId: number;
    inviteCode: string;
    status: RoomStatus;
}

export interface RoomStateResponse {
    id: number;
    status: RoomStatus;
    numberOfRounds: number;
    durationOfRound: number;
    inviteCode: string;
    creatorId: number;
    players: Array<{ userId: number; score: number }>;
    currentRound?: number;
    currentDrawerId?: number;
    secondsLeft?: number;
    wordToGuess?: string | null;
    pixels?: Pixel[];
}

export interface SubmitPixelsRequest {
    points: Pixel[];
}

export interface ResultsResponse {
    players: Array<{ userId: number; score: number }>;
}

export interface JoinByInviteCodeResponse {
    roomId: number;
    status: string;
    score: number;
}

export interface GuessRequest {
    guess: string;
}

export interface GuessResponse {
    correct: boolean;
    roundNumber: number;
    status: RoomStatus;
}

