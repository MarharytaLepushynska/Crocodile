package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.User;
import utils.DataBase;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// POST /register  — реєстрація
// POST /login     — вхід
public class AuthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            if (!RouterUtil.methodIs(ex, "POST")) {
                RouterUtil.sendError(ex, 405, "Method Not Allowed");
                return;
            }
            if (path.endsWith("/register")) {
                handleRegister(ex);
            } else if (path.endsWith("/login")) {
                handleLogin(ex);
            } else {
                RouterUtil.sendError(ex, 404, "Not Found");
            }
        } catch (Exception e) {
            RouterUtil.sendError(ex, 400, e.getMessage());
        }
    }

    // POST /register  body: {"username":"...","password":"...","avatar":"..."}
    private void handleRegister(HttpExchange ex) throws IOException {
        Map<String, String> body = RouterUtil.parseBody(ex);
        String username = body.get("username");
        String password = body.get("password");
        String avatar   = body.getOrDefault("avatar", "default");

        User user = DataBase.getUserService().create(username, password, avatar);
        RouterUtil.sendCreated(ex, userMap(user));
    }

    // POST /login  body: {"username":"...","password":"..."}
    private void handleLogin(HttpExchange ex) throws IOException {
        Map<String, String> body = RouterUtil.parseBody(ex);
        User user = DataBase.getUserService().authenticate(body.get("username"), body.get("password"));
        if (user == null) {
            RouterUtil.sendError(ex, 401, "invalid credentials");
            return;
        }
        RouterUtil.sendOk(ex, userMap(user));
    }

    private Map<String, Object> userMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",       u.getId());
        m.put("username", u.getUsername());
        m.put("avatar",   u.getAvatar());
        return m;
    }
}
