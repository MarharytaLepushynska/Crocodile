package httpConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.User;
import service.UserService;
import utils.JsonUtil;
import utils.JwtUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final UserService userService;
    private final boolean isRegistered;

    public AuthHandler(UserService userService, boolean isRegistered) {
        this.userService = userService;
        this.isRegistered = isRegistered;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("OPTIONS")) {
            HttpResponsesMaker.sendNoContext(exchange);
            return;
        }

        if(!method.equals("POST")) {
            HttpResponsesMaker.sendError(exchange, 405, "POST expected");
            return;
        }

        try {
            Map<String, String> body = JsonUtil.parseObject(HttpResponsesMaker.readBody(exchange));
            String username = body.get("username");
            String password = body.get("password");

            User user = isRegistered ? handleRegister(username, password, body.get("avatarFileName"))
                    : handleLogin(username, password);

            if(user == null) {
                HttpResponsesMaker.sendError(exchange, 401, "Wrong login or password");
                return;
            }

            String token = JwtUtil.generateToken(user.getId(), user.getUsername());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("avatarFileName", user.getAvatar());

            HttpResponsesMaker.sendJson(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            HttpResponsesMaker.sendError(exchange, 400, e.getMessage());
        } catch (RuntimeException e) {
            HttpResponsesMaker.sendError(exchange, 409, e.getMessage());
        }
    }

    private User handleRegister(String username, String password, String avatarFileName) {
        return userService.create(username, password, avatarFileName);
    }

    private User handleLogin(String username, String password) {
        return userService.authenticate(username, password);
    }
}
