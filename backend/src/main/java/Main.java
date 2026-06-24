import httpConnection.HttpServerLauncher;
import utils.DataBase;

import java.util.TimeZone;

public class Main {
    public static void main(String[] args) throws Exception {
        // примусово ініціалізує DataBase -> UserService/RoomService -> createTables()

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
        DataBase.getUserService();

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080")
        );
        //int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServerLauncher.start(port);
    }
}
