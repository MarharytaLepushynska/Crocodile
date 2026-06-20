package utils;

import game.GameSessionRegistry;
import service.GameService;
import service.RoomService;
import service.UserService;
import service.WordService;

public class DataBase {
    // локально: export DATABASE_URL=jdbc:postgresql://localhost:5432/crocodile?user=postgres&password=...
    // Railway/Render: DATABASE_URL виставляється автоматично
    private static final String DEFAULT_DB_URL =
            System.getenv().getOrDefault("DATABASE_URL", "jdbc:postgresql://localhost:5432/crocodile");

    private static final UserService USER_SERVICE = new UserService(DEFAULT_DB_URL);
    private static final RoomService ROOM_SERVICE = new RoomService(DEFAULT_DB_URL);
    private static final WordService WORD_SERVICE = new WordService();
    private static final GameSessionRegistry GAME_SESSION_REGISTRY = new GameSessionRegistry();
    private static final GameService GAME_SERVICE =
            new GameService(ROOM_SERVICE, WORD_SERVICE, GAME_SESSION_REGISTRY);

    public static UserService getUserService() {
        return USER_SERVICE;
    }

    public static RoomService getRoomService() {
        return ROOM_SERVICE;
    }

    public static WordService getWordService() {
        return WORD_SERVICE;
    }

    public static GameService getGameService() {
        return GAME_SERVICE;
    }
}