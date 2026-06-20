package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.Room;
import enteties.RoomUser;
import utils.DataBase;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// POST /join  body: {"inviteCode":"abc12345"}
// Альтернативний спосіб приєднатись за invite-кодом замість roomId у шляху
public class JoinRoomHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!RouterUtil.methodIs(ex, "POST")) {
                RouterUtil.sendError(ex, 405, "Method Not Allowed");
                return;
            }
            long userId = RouterUtil.getUserId(ex);
            Map<String, String> body = RouterUtil.parseBody(ex);
            String code = body.get("inviteCode");
            if (code == null || code.isBlank()) {
                RouterUtil.sendError(ex, 400, "inviteCode required");
                return;
            }

            Room room = DataBase.getRoomService().findByInviteCode(code.trim());
            if (room == null) {
                RouterUtil.sendError(ex, 404, "room not found for invite code: " + code);
                return;
            }

            RoomUser ru = DataBase.getRoomService().join(room.getId(), userId);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("roomId",     room.getId());
            resp.put("inviteCode", room.getInviteCode());
            resp.put("userId",     ru.getUserId());
            resp.put("role",       ru.getRole().name());
            resp.put("score",      ru.getScore());
            RouterUtil.sendOk(ex, resp);
        } catch (Exception e) {
            RouterUtil.sendError(ex, 400, e.getMessage());
        }
    }
}
