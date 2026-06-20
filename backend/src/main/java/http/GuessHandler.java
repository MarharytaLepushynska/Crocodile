package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.GameService;
import utils.DataBase;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// POST /guess  body: {"roomId":1,"guess":"кошеня"}  (X-User-Id = guesser)
public class GuessHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!RouterUtil.methodIs(ex, "POST")) {
                RouterUtil.sendError(ex, 405, "Method Not Allowed");
                return;
            }
            long userId = RouterUtil.getUserId(ex);
            Map<String, String> body = RouterUtil.parseBody(ex);

            String roomIdStr = body.get("roomId");
            String guess     = body.get("guess");
            if (roomIdStr == null || guess == null) {
                RouterUtil.sendError(ex, 400, "roomId and guess required");
                return;
            }

            long roomId = Long.parseLong(roomIdStr);

            // перевіряємо таймаут перед обробкою відгадки
            DataBase.getGameService().checkRoundTimeout(roomId);

            GameService.GuessResult result = DataBase.getGameService().submitGuess(roomId, userId, guess);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("correct",      result.correct());
            resp.put("roundNumber",  result.roundNumber());
            resp.put("gameStatus",   result.status().name());
            if (result.correct()) {
                resp.put("message", "Правильно! +1 бал");
            } else {
                resp.put("message", "Невірно, спробуй ще раз");
            }
            RouterUtil.sendOk(ex, resp);

        } catch (Exception e) {
            RouterUtil.sendError(ex, 400, e.getMessage());
        }
    }
}
