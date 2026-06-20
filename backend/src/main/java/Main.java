import http.HttpServerLauncher;
import utils.DataBase;

public class Main {
    public static void main(String[] args) throws Exception {
        // примусово ініціалізує DataBase -> UserService/RoomService -> createTables()
        DataBase.getUserService();

        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServerLauncher.start(port);
    }
}
