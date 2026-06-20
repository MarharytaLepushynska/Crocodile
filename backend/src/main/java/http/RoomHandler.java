package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.Room;
import enteties.RoomUser;
import enums.RoomStatus;
import game.GameSession;
import utils.DataBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// POST /rooms              — створити кімнату  (X-User-Id = creator)
// GET  /rooms/{id}         — стан кімнати + гравці
// POST /rooms/{id}/join    — приєднатись
// POST /rooms/{id}/start   — почати гру (тільки creator)
// POST /rooms/{id}/finish  — завершити достроково (тільки creator)
// GET  /rooms/{id}/results — фінальні результати
public class RoomHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String[] segs = RouterUtil.pathSegments(ex);
            // segs[0] = "rooms"
            if (segs.length == 1) {
                // /rooms
                if (RouterUtil.methodIs(ex, "POST")) createRoom(ex);
                else RouterUtil.sendError(ex, 405, "Method Not Allowed");

            } else if (segs.length == 2) {
                // /rooms/{id}
                long roomId = Long.parseLong(segs[1]);
                if (RouterUtil.methodIs(ex, "GET")) getRoom(ex, roomId);
                else RouterUtil.sendError(ex, 405, "Method Not Allowed");

            } else if (segs.length == 3) {
                long roomId = Long.parseLong(segs[1]);
                switch (segs[2]) {
                    case "join"    -> { if (RouterUtil.methodIs(ex, "POST")) joinRoom(ex, roomId);
                                        else RouterUtil.sendError(ex, 405, "Method Not Allowed"); }
                    case "start"   -> { if (RouterUtil.methodIs(ex, "POST")) startGame(ex, roomId);
                                        else RouterUtil.sendError(ex, 405, "Method Not Allowed"); }
                    case "finish"  -> { if (RouterUtil.methodIs(ex, "POST")) finishGame(ex, roomId);
                                        else RouterUtil.sendError(ex, 405, "Method Not Allowed"); }
                    case "results" -> { if (RouterUtil.methodIs(ex, "GET")) getResults(ex, roomId);
                                        else RouterUtil.sendError(ex, 405, "Method Not Allowed"); }
                    case "pixels"  -> new PixelsHandler().handle(ex);
                    default        -> RouterUtil.sendError(ex, 404, "Not Found");
                }
            } else {
                RouterUtil.sendError(ex, 404, "Not Found");
            }
        } catch (NumberFormatException e) {
            RouterUtil.sendError(ex, 400, "invalid room id");
        } catch (Exception e) {
            RouterUtil.sendError(ex, 400, e.getMessage());
        }
    }

    // POST /rooms  body: {"numberOfRounds":3,"durationOfRound":60}
    private void createRoom(HttpExchange ex) throws IOException {
        long creatorId = RouterUtil.getUserId(ex);
        Map<String, String> body = RouterUtil.parseBody(ex);
        int rounds   = Integer.parseInt(body.getOrDefault("numberOfRounds",   "3"));
        int duration = Integer.parseInt(body.getOrDefault("durationOfRound", "60"));

        Room room = DataBase.getRoomService().createRoom(creatorId, rounds, duration);
        RouterUtil.sendCreated(ex, roomMap(room, DataBase.getRoomService().getRoomUsers(room.getId())));
    }

    // GET /rooms/{id}
    private void getRoom(HttpExchange ex, long roomId) throws IOException {
        Room room = DataBase.getRoomService().findById(roomId);
        if (room == null) { RouterUtil.sendError(ex, 404, "room not found"); return; }

        List<RoomUser> players = DataBase.getRoomService().getRoomUsers(roomId);

        Map<String, Object> resp = roomMap(room, players);

        // якщо гра йде — додаємо поточний раунд і роль запитувача
        try {
            long userId = RouterUtil.getUserId(ex);
            GameSession session = DataBase.getGameService().getSession(roomId);
            resp.put("currentRound",    session.getCurrentRoundNumber());
            resp.put("totalRounds",     session.getTotalRounds());
            resp.put("roundExpiresAt",  session.getRoundStartedAtMillis() + (long) session.getRoundDurationSec() * 1000);
            resp.put("myRole",          session.getRole(userId).name());
            // слово видається тільки художнику
            if (session.getCurrentDrawerId() == userId)
                resp.put("currentWord", session.getCurrentWord());
        } catch (Exception ignored) {
            // сесії ще немає або немає заголовка — не критично
        }

        RouterUtil.sendOk(ex, resp);
    }

    // POST /rooms/{id}/join
    private void joinRoom(HttpExchange ex, long roomId) throws IOException {
        long userId = RouterUtil.getUserId(ex);
        RoomUser ru = DataBase.getRoomService().join(roomId, userId);
        RouterUtil.sendOk(ex, ruMap(ru));
    }

    // POST /rooms/{id}/start
    private void startGame(HttpExchange ex, long roomId) throws IOException {
        long userId = RouterUtil.getUserId(ex);
        GameSession session = DataBase.getGameService().startGame(roomId, userId);
        RouterUtil.sendOk(ex, sessionMap(session, userId));
    }

    // POST /rooms/{id}/finish
    private void finishGame(HttpExchange ex, long roomId) throws IOException {
        long userId = RouterUtil.getUserId(ex);
        DataBase.getGameService().forceFinish(roomId, userId);
        RouterUtil.sendOk(ex, Map.of("status", RoomStatus.FINISHED.name()));
    }

    // GET /rooms/{id}/results
    private void getResults(HttpExchange ex, long roomId) throws IOException {
        List<RoomUser> results = DataBase.getGameService().getResults(roomId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (RoomUser ru : results) list.add(ruMap(ru));
        RouterUtil.sendOk(ex, list);
    }

    private Map<String, Object> roomMap(Room r, List<RoomUser> players) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              r.getId());
        m.put("creatorId",       r.getCreatorId());
        m.put("numberOfRounds",  r.getNumberOfRounds());
        m.put("durationOfRound", r.getDurationOfRound());
        m.put("inviteCode",      r.getInviteCode());
        m.put("status",          r.getStatus().name());
        List<Map<String, Object>> pList = new ArrayList<>();
        for (RoomUser ru : players) pList.add(ruMap(ru));
        m.put("players", pList);
        return m;
    }

    private Map<String, Object> ruMap(RoomUser ru) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", ru.getUserId());
        m.put("role",   ru.getRole().name());
        m.put("score",  ru.getScore());
        return m;
    }

    private Map<String, Object> sessionMap(GameSession s, long userId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("roomId",       s.getRoomId());
        m.put("status",       s.getStatus().name());
        m.put("currentRound", s.getCurrentRoundNumber());
        m.put("totalRounds",  s.getTotalRounds());
        m.put("myRole",       s.getRole(userId).name());
        if (s.getCurrentDrawerId() == userId)
            m.put("currentWord", s.getCurrentWord());
        return m;
    }
}
