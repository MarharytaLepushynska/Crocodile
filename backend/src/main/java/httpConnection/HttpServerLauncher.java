package httpConnection;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import utils.DataBase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServerLauncher {
    private HttpServer server;

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        //пул з потоками щоб один запит не тормозив інші, мб більший треба буде
        server.setExecutor(Executors.newFixedThreadPool(20));

        server.createContext("/register", new AuthHandler(DataBase.getUserService(), true));
        server.createContext("/login", new AuthHandler(DataBase.getUserService(), false));

        // user
        HttpContext usersContext = server.createContext("/user", new UserHandler(DataBase.getUserService()));
        usersContext.setAuthenticator(new JwtAuthenticator());

        // rooms (охоплює /rooms, /rooms/{id}, /rooms/{id}/join, /rooms/{id}/start,
        //                /rooms/{id}/finish, /rooms/{id}/results, /rooms/{id}/pixels)
        HttpContext roomsContext = server.createContext("/rooms", new RoomHandler(DataBase.getRoomService(), DataBase.getGameService()));
        roomsContext.setAuthenticator(new JwtAuthenticator());

        // join by invite code
        HttpContext joinContext = server.createContext("/join", new JoinRoomHandler(DataBase.getRoomService()));
        joinContext.setAuthenticator(new JwtAuthenticator());

        // guess
        HttpContext guessContext = server.createContext("/guess", new GuessHandler(DataBase.getGameService()));
        guessContext.setAuthenticator(new JwtAuthenticator());

        server.start();
        System.out.println("Http сервер запущено на порту: " + port);
    }

    public void stop() {
        if(server != null) {
            server.stop(0);
        }
    }
}
