package enteties;

import enums.RoomStatus;

public class Room {
    private long id;
    private long creatorId;       // FK User (as Creator / адміністратор кімнати)
    private int numberOfRounds;
    private int durationOfRound;   // у секундах
    private String inviteCode;     // унікальний код для запрошення за посиланням
    private RoomStatus status;

    public Room() {
        this.status = RoomStatus.WAITING;
    }

    public Room(long creatorId, int numberOfRounds, int durationOfRound, String inviteCode) {
        this.creatorId = creatorId;
        this.numberOfRounds = numberOfRounds;
        this.durationOfRound = durationOfRound;
        this.inviteCode = inviteCode;
        this.status = RoomStatus.WAITING;
    }

    public Room(long id, long creatorId, int numberOfRounds, int durationOfRound, String inviteCode, RoomStatus status) {
        this.id = id;
        this.creatorId = creatorId;
        this.numberOfRounds = numberOfRounds;
        this.durationOfRound = durationOfRound;
        this.inviteCode = inviteCode;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    public int getDurationOfRound() {
        return durationOfRound;
    }

    public void setDurationOfRound(int durationOfRound) {
        this.durationOfRound = durationOfRound;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}