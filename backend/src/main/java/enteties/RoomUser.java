package enteties;

import enums.PlayerRole;

public class RoomUser {
    private long id;
    private long roomId;  // FK Кімната
    private long userId;  // FK Юзер
    private PlayerRole role; // DRAWER / GUESSER, актуально лише під час раунду
    private int score;

    public RoomUser() {
    }

    public RoomUser(long roomId, long userId) {
        this.roomId = roomId;
        this.userId = userId;
        this.role = PlayerRole.GUESSER;
        this.score = 0;
    }

    public RoomUser(long id, long roomId, long userId, PlayerRole role, int score) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
        this.score = score;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }
}