package http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServerLauncher {

    private static final int DEFAULT_PORT = 8080;

    public static void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // auth
        server.createContext("/register", new AuthHandler());
        server.createContext("/login",    new AuthHandler());

        // user
        server.createContext("/user",     new UserHandler());

        // rooms (охоплює /rooms, /rooms/{id}, /rooms/{id}/join, /rooms/{id}/start,
        //                /rooms/{id}/finish, /rooms/{id}/results, /rooms/{id}/pixels)
        server.createContext("/rooms",    new RoomHandler());

        // join by invite code
        server.createContext("/join",     new JoinRoomHandler());

        // guess
        server.createContext("/guess",    new GuessHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("HTTP server started on port " + port);
        System.out.println("Endpoints:");
        System.out.println("  POST   /register");
        System.out.println("  POST   /login");
        System.out.println("  GET    /user/{id}");
        System.out.println("  PUT    /user/{id}/avatar");
        System.out.println("  POST   /rooms                 (X-User-Id: <creatorId>)");
        System.out.println("  GET    /rooms/{id}            (X-User-Id: <userId>)");
        System.out.println("  POST   /rooms/{id}/join       (X-User-Id: <userId>)");
        System.out.println("  POST   /rooms/{id}/start      (X-User-Id: <creatorId>)");
        System.out.println("  POST   /rooms/{id}/finish     (X-User-Id: <creatorId>)");
        System.out.println("  GET    /rooms/{id}/results");
        System.out.println("  PUT    /rooms/{id}/pixels     (X-User-Id: <drawerId>, body: pixel-batch)");
        System.out.println("  GET    /rooms/{id}/pixels");
        System.out.println("  POST   /join                  (X-User-Id: <userId>, body: {inviteCode})");
        System.out.println("  POST   /guess                 (X-User-Id: <userId>, body: {roomId, guess})");
    }
}
