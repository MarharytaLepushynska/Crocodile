package utils;

import game.GameSessionRegistry;
import service.GameService;
import service.RoomService;
import service.UserService;
import service.WordService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataBase {
    private static final String DEFAULT_DB_URL = resolveDbUrl();

    private static String resolveDbUrl() {
        String envUrl = System.getenv("DATABASE_URL");
        if (envUrl != null && !envUrl.isBlank()) return envUrl;

        try (InputStream in = DataBase.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String url = props.getProperty("db.url");
                if (url != null && !url.isBlank()) return url;
            }
        } catch (IOException ignored) {}

        return "jdbc:postgresql://localhost:5432/crocodile";
    }

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