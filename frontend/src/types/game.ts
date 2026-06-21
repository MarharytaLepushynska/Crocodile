export type RoomStatus = "WAITING" | "IN_PROGRESS" | "FINISHED";

export interface Avatar {
    fileName: string;
}

export const AVAILABLE_AVATARS: Avatar[] = [
    { fileName: "avatar1.png" },
    { fileName: "avatar2.png" },
    { fileName: "avatar3.png" },
    { fileName: "avatar4.png" },
    { fileName: "avatar5.png" },
];

export const DRAW_COLORS: string[] = [
    "#000000", // 0 BLACK
    "#FF0000", // 1 RED
    "#00A300", // 2 GREEN
    "#0066FF", // 3 BLUE
    "#FFD500", // 4 YELLOW
    "#FFFFFF", // 5 WHITE (стирачка)
];

export interface Pixel {
    x: number;
    y: number;
    colorIndex: number;
}

//get/rooms/{id}
export interface RoomPlayer {
    userId: number;
    score: number;
}

//get/user/{id}
export interface UserProfile {
    userId: number;
    username: string;
    avatarFileName: string;
}

//повноцінний гравець для відображення
export interface DisplayPlayer {
    userId: number;
    username: string;
    avatarFileName: string;
    score: number;
    isAdmin: boolean;
    isDrawer: boolean;
}

//get/rooms/{id}
export interface Room {
    id: number;
    status: RoomStatus;
    numberOfRounds: number;
    durationOfRound: number;
    inviteCode: string;
    creatorId: number;
    players: RoomPlayer[];

    currentRound?: number;
    currentDrawerId?: number;
    secondsLeft?: number;

    wordToGuess?: string | null;
    pixels?: Pixel[];
}
