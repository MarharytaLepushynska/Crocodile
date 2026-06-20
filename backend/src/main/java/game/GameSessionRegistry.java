package game;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameSessionRegistry {
    private final Map<Long, GameSession> sessions = new ConcurrentHashMap<>();

    public void put(GameSession session) {
        sessions.put(session.getRoomId(), session);
    }

    public GameSession get(long roomId) {
        return sessions.get(roomId);
    }

    public void remove(long roomId) {
        sessions.remove(roomId);
    }

    public boolean exists(long roomId) {
        return sessions.containsKey(roomId);
    }
}