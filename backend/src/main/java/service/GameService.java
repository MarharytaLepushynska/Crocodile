package service;

import enteties.Room;
import enteties.RoomUser;
import enums.RoomStatus;
import game.GameSession;
import game.GameSessionRegistry;
import packet.PixelBatch;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
//ми будемо використовувати цей сервіс у http-обробниках для запуску гри, прийому малюнків та відгадок, отримання результатів

public class GameService {
    private final RoomService roomService;
    private final WordService wordService;
    private final GameSessionRegistry registry;

    public GameService(RoomService roomService, WordService wordService, GameSessionRegistry registry) {
        this.roomService = roomService;
        this.wordService = wordService;
        this.registry = registry;
    }

    public GameSession startGame(long roomId, long requesterId) {
        if (!roomService.isCreator(roomId, requesterId))
            throw new RuntimeException("only the room creator can start the game");

        Room room = roomService.findById(roomId);
        if (room == null)
            throw new RuntimeException("room not found: " + roomId);

        List<RoomUser> players = roomService.getRoomUsers(roomId);
        if (players.size() < 2)
            throw new RuntimeException("need at least 2 players to start");

        GameSession session = new GameSession(roomId, room.getNumberOfRounds(), room.getDurationOfRound(), players);
        registry.put(session);
        roomService.setStatus(roomId, RoomStatus.IN_PROGRESS);

        session.startRound(wordService.getRandomWord());
        return session;
    }

    public GameSession getSession(long roomId) {
        GameSession session = registry.get(roomId);
        if (session == null)
            throw new RuntimeException("no active game in room: " + roomId);

        return session;
    }

    public void submitPixels(long roomId, long userId, List<PixelBatch.Pixel> pixels) {
        getSession(roomId).applyPixels(userId, pixels);
    }

    public GuessResult submitGuess(long roomId, long userId, String guess) {
        GameSession session = getSession(roomId);

        if (userId == session.getCurrentDrawerId())
            throw new RuntimeException("drawer cannot guess");

        boolean correct = session.checkGuess(guess);
        if (correct) {
            awardPoint(roomId, userId);
            advanceRoundOrFinish(roomId, session);
        }

        return new GuessResult(correct, session.getCurrentRoundNumber(), session.getStatus());
    }

    public void checkRoundTimeout(long roomId) {
        GameSession session = getSession(roomId);
        if (session.getStatus() == RoomStatus.IN_PROGRESS && session.isRoundExpired())
            advanceRoundOrFinish(roomId, session);
    }

    public void forceFinish(long roomId, long requesterId) {
        if (!roomService.isCreator(roomId, requesterId))
            throw new RuntimeException("only the room creator can finish the game");

        getSession(roomId).finish();
        roomService.setStatus(roomId, RoomStatus.FINISHED);
    }

    public List<RoomUser> getResults(long roomId) {
        return roomService.getRoomUsers(roomId).stream()
                .sorted(Comparator.comparingInt(RoomUser::getScore).reversed())
                .toList();
    }

    private void awardPoint(long roomId, long userId) {
        roomService.addScore(roomId, userId, 1);
    }

    private void advanceRoundOrFinish(long roomId, GameSession session) {
        if (session.hasMoreRounds()) {
            session.startRound(wordService.getRandomWordExcluding(session.getUsedWords()));
        } else {
            session.finish();
            roomService.setStatus(roomId, RoomStatus.FINISHED);
        }
    }

    public record GuessResult(boolean correct, int roundNumber, RoomStatus status) {
    }
}
