package httpConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enteties.Room;
import enteties.RoomUser;
import enums.RoomStatus;
import game.GameSession;
import packet.PixelBatch;
import service.GameService;
import service.RoomService;
import utils.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoomHandler implements HttpHandler {
    private final RoomService roomService;
    private final GameService gameService;

    public RoomHandler (RoomService roomService, GameService gameService) {
        this.roomService = roomService;
        this.gameService = gameService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");

        if(segments.length == 2 && method.equals("POST")) {
            long userId = AuthContext.getUserId(exchange);
            Map<String, String> body = JsonUtil.parseObject(HttpResponsesMaker.readBody(exchange));

            int rounds = Integer.parseInt(body.get("numberOfRounds"));
            int duration = Integer.parseInt(body.get("durationOfRound"));

            Room room = roomService.createRoom(userId, rounds, duration);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("roomId", room.getId());
            response.put("inviteCode", room.getInviteCode());
            response.put("status", room.getStatus().name());

            HttpResponsesMaker.sendJson(exchange, 200, response);
            return;
        }

        if (segments.length == 3 && method.equals("GET")) {
            long roomId = Long.parseLong(segments[2]);
            long userId = AuthContext.getUserId(exchange);

            Room room = roomService.findById(roomId);
            if (room == null) {
                HttpResponsesMaker.sendError(exchange, 404, "Room not found");
                return;
            }

            if (room.getStatus() == RoomStatus.IN_PROGRESS) {
                gameService.checkRoundTimeout(roomId);
                room = roomService.findById(roomId);
            }

            Map<String, Object> response = makeRoomStateResponse(room, userId);
            HttpResponsesMaker.sendJson(exchange, 200, response);
            return;
        }

        if(segments.length == 4 && method.equals("POST") && segments[3].equals("join")) {
            long roomId = Long.parseLong(segments[2]);
            long userId = AuthContext.getUserId(exchange);

            try {
                roomService.join(roomId, userId);
                HttpResponsesMaker.sendJson(exchange, 200, Map.of("status", "joined"));
            } catch (RuntimeException e) {
                HttpResponsesMaker.sendError(exchange, 409, e.getMessage());
            }
            return;
        }

        if(segments.length == 4 && method.equals("POST") && segments[3].equals("start")) {
            long roomId = Long.parseLong(segments[2]);
            long userId = AuthContext.getUserId(exchange);

            try {
                gameService.startGame(roomId, userId);
                HttpResponsesMaker.sendJson(exchange, 200, Map.of("status", "started"));
            } catch (RuntimeException e) {
                HttpResponsesMaker.sendError(exchange, 403, e.getMessage());
            }
            return;
        }

        if (segments.length == 4 && method.equals("PUT") && segments[3].equals("pixels")) {
            long roomId = Long.parseLong(segments[2]);
            long userId = AuthContext.getUserId(exchange);

            List<PixelBatch.Pixel> pixels = parsePixelsFromBody(HttpResponsesMaker.readBody(exchange));

            try {
                gameService.submitPixels(roomId, userId, pixels);
                HttpResponsesMaker.sendJson(exchange, 200, Map.of("status", "ok"));
            } catch (RuntimeException e) {
                HttpResponsesMaker.sendError(exchange, 403, e.getMessage());
            }
            return;
        }

        if (segments.length == 4 && method.equals("POST") && segments[3].equals("finish")) {
            long roomId = Long.parseLong(segments[2]);
            long userId = AuthContext.getUserId(exchange);

            try {
                gameService.forceFinish(roomId, userId);
                HttpResponsesMaker.sendJson(exchange, 200, Map.of("status", "finished"));
            } catch (RuntimeException e) {
                HttpResponsesMaker.sendError(exchange, 403, e.getMessage());
            }
            return;
        }

        if(segments.length == 4 && method.equals("GET") && segments[3].equals("results")) {
            long roomId = Long.parseLong(segments[2]);

            List<RoomUser> results = gameService.getResults(roomId);

            List<Map<String, Object>> playersJson = new ArrayList<>();
            for(RoomUser roomUser: results) {
                Map<String, Object> playerJson = new LinkedHashMap<>();
                playerJson.put("userId", roomUser.getUserId());
                playerJson.put("score", roomUser.getScore());
                playersJson.add(playerJson);
            }

            HttpResponsesMaker.sendJson(exchange, 200, Map.of("players", playersJson));
            return;
        }

        HttpResponsesMaker.sendError(exchange, 404, "Unknown route: " + method + " " + path);
    }

    private List<PixelBatch.Pixel> parsePixelsFromBody(String body) {
        List<Map<String, String>> rawPoints= JsonUtil.parseArrayOfObjects(body, "points");
        List<PixelBatch.Pixel> pixels = new ArrayList<>();

        for (Map<String, String> point: rawPoints) {
            int x = Integer.parseInt(point.get("x"));
            int y = Integer.parseInt(point.get("y"));
            int colorIndex = Integer.parseInt(point.get("colorIndex"));
            pixels.add(new PixelBatch.Pixel(x, y, colorIndex));
        }
        return pixels;
    }

    private Map<String, Object> makeRoomStateResponse(Room room, long userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", room.getId());
        response.put("status", room.getStatus().name());
        response.put("numberOfRounds", room.getNumberOfRounds());
        response.put("durationOfRound", room.getDurationOfRound());
        response.put("inviteCode", room.getInviteCode());
        response.put("creatorId", room.getCreatorId());

        List<RoomUser> roomUsers = roomService.getRoomUsers(room.getId());
        List<Map<String, Object>> playersJson = new ArrayList<>();
        for(RoomUser roomUser: roomUsers) {
            Map<String, Object> playerJson = new LinkedHashMap<>();
            playerJson.put("userId", roomUser.getUserId());
            playerJson.put("score", roomUser.getScore());
            playersJson.add(playerJson);
        }
        response.put("players", playersJson);

        if(room.getStatus() == RoomStatus.IN_PROGRESS) {
            try {
                GameSession session = gameService.getSession(room.getId());
                response.put("currentRound", session.getCurrentRoundNumber());
                response.put("currentDrawerId", session.getCurrentDrawerId());

                long elapsedSec = (System.currentTimeMillis() - session.getRoundStartedAtMillis()) / 1000;
                long secondsLeft = Math.max(0, session.getRoundDurationSec() - elapsedSec);
                response.put("secondsLeft", secondsLeft);

                if(userId == session.getCurrentDrawerId()) {
                    response.put("wordToGuess", session.getCurrentWord());
                } else {
                    response.put("wordToGuess", null);
                }

                List<Map<String, Object>> pixelsJson = new ArrayList<>();
                for(PixelBatch.Pixel p: session.getCanvas()) {
                    Map<String, Object> pixelJson = new LinkedHashMap<>();
                    pixelJson.put("x", p.getX());
                    pixelJson.put("y", p.getY());
                    pixelJson.put("colorIndex", p.getColorIndex());
                    pixelsJson.add(pixelJson);
                }
                response.put("pixels", pixelsJson);
            } catch (RuntimeException e) {}
        }

        return response;
    }
}
