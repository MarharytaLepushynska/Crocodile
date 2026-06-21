package httpConnection;

import com.sun.net.httpserver.HttpServer;
import utils.DataBase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServerLauncher {
    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        //пул з потоками щоб один запит не тормозив інші, мб більший треба буде
        server.setExecutor(Executors.newFixedThreadPool(20));

        server.createContext("/register", new AuthHandler(DataBase.getUserService(), true));
        server.createContext("/login", new AuthHandler(DataBase.getUserService(), false));

        //TODO: інші захищені шляхи

        server.start();
        System.out.println("Http сервер запущено на порту: " + port);
    }
}
