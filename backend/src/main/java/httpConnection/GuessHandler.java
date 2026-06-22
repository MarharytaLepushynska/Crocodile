package httpConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.GameService;
import utils.JsonUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuessHandler implements HttpHandler {
    private final GameService gameService;

    public GuessHandler (GameService gameService) {
        this.gameService = gameService;
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

        if(segments.length == 3 && method.equals("POST")) {
            long roomId = Long.parseLong(segments[2]);
            long userId = AuthContext.getUserId(exchange);

            Map<String, String> body = JsonUtil.parseObject(HttpResponsesMaker.readBody(exchange));
            String guess = body.get("guess");

            if (guess == null || guess.isBlank()) {
                HttpResponsesMaker.sendError(exchange, 400, "guess cannot be empty");
                return;
            }

            try {
                GameService.GuessResult result = gameService.submitGuess(roomId, userId, guess);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("correct", result.correct());
                response.put("roundNumber", result.roundNumber());
                response.put("status", result.status().name());

                HttpResponsesMaker.sendJson(exchange, 200, response);
            } catch (RuntimeException e) {
                HttpResponsesMaker.sendError(exchange, 403, e.getMessage());
            }

            return;
        }

        HttpResponsesMaker.sendError(exchange, 404, "Unknown route: " + method + " " + path);
    }
}
