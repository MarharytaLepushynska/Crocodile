import httpConnection.HttpServerLauncher;
import utils.DataBase;

import java.time.ZoneId;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) throws Exception {
        // примусово ініціалізує DataBase -> UserService/RoomService -> createTables()
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("user.timezone = " + System.getProperty("user.timezone"));
        System.out.println("default timezone = " + TimeZone.getDefault());
        System.out.println("ZoneId = " + ZoneId.systemDefault());

        System.out.println(System.getenv("DATABASE_URL"));
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
        DataBase.getUserService();

        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServerLauncher.start(port);
    }
}
