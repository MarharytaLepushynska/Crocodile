package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.User;
import utils.DataBase;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// GET  /user/{id}        — отримати профіль
// PUT  /user/{id}/avatar — оновити аватар  (X-User-Id має збігатись з {id})
public class UserHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String[] segs = RouterUtil.pathSegments(ex);
            // segs: ["user", "{id}"] або ["user", "{id}", "avatar"]
            if (segs.length < 2) { RouterUtil.sendError(ex, 400, "missing user id"); return; }

            long id = Long.parseLong(segs[1]);

            if (segs.length == 2 && RouterUtil.methodIs(ex, "GET")) {
                getUser(ex, id);
            } else if (segs.length == 3 && segs[2].equals("avatar") && RouterUtil.methodIs(ex, "PUT")) {
                updateAvatar(ex, id);
            } else {
                RouterUtil.sendError(ex, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            RouterUtil.sendError(ex, 400, e.getMessage());
        }
    }

    private void getUser(HttpExchange ex, long id) throws IOException {
        User user = DataBase.getUserService().findById(id);
        if (user == null) { RouterUtil.sendError(ex, 404, "user not found"); return; }
        RouterUtil.sendOk(ex, toMap(user));
    }

    private void updateAvatar(HttpExchange ex, long id) throws IOException {
        long requesterId = RouterUtil.getUserId(ex);
        if (requesterId != id) { RouterUtil.sendError(ex, 403, "forbidden"); return; }

        Map<String, String> body = RouterUtil.parseBody(ex);
        String avatar = body.get("avatar");
        User updated = DataBase.getUserService().updateAvatar(id, avatar);
        if (updated == null) { RouterUtil.sendError(ex, 404, "user not found"); return; }
        RouterUtil.sendOk(ex, toMap(updated));
    }

    private Map<String, Object> toMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",       u.getId());
        m.put("username", u.getUsername());
        m.put("avatar",   u.getAvatar());
        return m;
    }
}
