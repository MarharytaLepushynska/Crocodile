package game;

import enteties.RoomUser;
import enums.PlayerRole;
import enums.RoomStatus;
import packet.PixelBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//тип даниз для гри, що зберігає стан кімнати (гравців, раундів, поточного художника, слова, малюнку)
//будемо користуватися цим класом у RoomManager, щоб зберігати стан гри у пам'яті
public class GameSession {
    private final long roomId;
    private final int totalRounds;
    private final int roundDurationSec;

    private RoomStatus status = RoomStatus.WAITING;
    private int currentRoundNumber = 0;
    private long currentDrawerId;
    private String currentWord;
    private long roundStartedAtMillis;

    private final List<Long> playerOrder = new ArrayList<>(); // userId у порядку, в якому стають художниками
    private final List<String> usedWords = new ArrayList<>();
    private final List<PixelBatch.Pixel> canvas = new CopyOnWriteArrayList<>();

    public GameSession(long roomId, int totalRounds, int roundDurationSec, List<RoomUser> players) {
        this.roomId = roomId;
        this.totalRounds = totalRounds;
        this.roundDurationSec = roundDurationSec;
        for (RoomUser ru : players)
            playerOrder.add(ru.getUserId());
    }

    public void startRound(String word) {
        currentRoundNumber++;
        currentWord = word;
        usedWords.add(word);
        canvas.clear();
        roundStartedAtMillis = System.currentTimeMillis();
        status = RoomStatus.IN_PROGRESS;

        // художник по черзі: round 1 -> playerOrder[0], round 2 -> playerOrder[1], ...
        int drawerIndex = (currentRoundNumber - 1) % playerOrder.size();
        currentDrawerId = playerOrder.get(drawerIndex);
    }

    public boolean isRoundExpired() {
        long elapsedSec = (System.currentTimeMillis() - roundStartedAtMillis) / 1000;
        return elapsedSec >= roundDurationSec;
    }

    public boolean hasMoreRounds() {
        return currentRoundNumber < totalRounds;
    }

    public void applyPixels(long userId, List<PixelBatch.Pixel> pixels) {
        if (userId != currentDrawerId)
            throw new RuntimeException("only the drawer can draw");

        canvas.addAll(pixels);
    }

    public List<PixelBatch.Pixel> getCanvas() {
        return canvas;
    }

    public PlayerRole getRole(long userId) {
        return userId == currentDrawerId ? PlayerRole.DRAWER : PlayerRole.GUESSER;
    }

    public boolean checkGuess(String guess) {
        return currentWord != null &&
                currentWord.strip().equalsIgnoreCase(guess.strip());
    }

    public void finish() {
        status = RoomStatus.FINISHED;
    }

    // геттери

    public long getRoomId() {
        return roomId;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public int getCurrentRoundNumber() {
        return currentRoundNumber;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public int getRoundDurationSec() {
        return roundDurationSec;
    }

    public long getCurrentDrawerId() {
        return currentDrawerId;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public List<String> getUsedWords() {
        return usedWords;
    }

    public long getRoundStartedAtMillis() {
        return roundStartedAtMillis;
    }
}