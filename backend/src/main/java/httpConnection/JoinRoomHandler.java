package httpConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.Room;
import enteties.RoomUser;
import service.RoomService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JoinRoomHandler implements HttpHandler {
    private final RoomService roomService;

    public JoinRoomHandler(RoomService roomService) {
        this.roomService = roomService;
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

        if (segments.length == 3 && method.equals("POST")) {
            String inviteCode = segments[2];
            long userId = AuthContext.getUserId(exchange);

            Room room = roomService.findByInviteCode(inviteCode);
            if(room == null) {
                HttpResponsesMaker.sendError(exchange, 404, "Invalid code");
                return;
            }

            try {
                RoomUser roomUser = roomService.join(room.getId(), userId);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("roomId", room.getId());
                response.put("status", "joined");
                response.put("score", roomUser.getScore());

                HttpResponsesMaker.sendJson(exchange, 200, response);
            } catch (RuntimeException e) {
                HttpResponsesMaker.sendError(exchange, 409, e.getMessage());
            }

            return;
        }

        HttpResponsesMaker.sendError(exchange, 404, "Unknown route: " + method + " " + path);
    }
}
