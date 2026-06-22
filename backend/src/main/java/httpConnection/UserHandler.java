package httpConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.User;
import service.UserService;
import utils.JsonUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserHandler implements HttpHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");

        if (method.equalsIgnoreCase("OPTIONS")) {
            HttpResponsesMaker.sendNoContext(exchange);
            return;
        }

        if(segments.length == 3 && method.equals("GET")) {
            long userId = Long.parseLong(segments[2]);

            User user = userService.findById(userId);
            if (user == null) {
                HttpResponsesMaker.sendError(exchange, 404, "User not found");
                return;
            }

            HttpResponsesMaker.sendJson(exchange, 200, makeUserResponse(user));
            return;
        }

        if (segments.length == 2 && method.equals("PUT")) {
            long userId = AuthContext.getUserId(exchange);
            Map<String, String> body = JsonUtil.parseObject(HttpResponsesMaker.readBody(exchange));
            String avatarFileName = body.get("avatarFileName");
            User updated = userService.updateAvatar(userId, avatarFileName);

            if (updated == null) {
                HttpResponsesMaker.sendError(exchange, 404, "User not found");
                return;
            }

            HttpResponsesMaker.sendJson(exchange, 200, makeUserResponse(updated));
            return;
        }

        HttpResponsesMaker.sendError(exchange, 404, "Unknown route: " + method + " " + path);
    }

    private Map<String, Object> makeUserResponse(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("avatarFileName", user.getAvatar());
        return response;
    }
}
