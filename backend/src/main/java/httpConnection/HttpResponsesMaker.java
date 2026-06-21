package httpConnection;

import com.sun.net.httpserver.HttpExchange;
import utils.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

//просто доп клас щоб не дублювати код для відповідей сервера
public class HttpResponsesMaker {
    private HttpResponsesMaker() {

    }

    public static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void sendJson(HttpExchange ex, int statusCode, Object body) throws IOException {
        String json = JsonUtil.toJson(body);
        byte[] bytes = json.getBytes();

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(statusCode, bytes.length);
        try(OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange ex, int statusCode, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        sendJson(ex, statusCode, body);
    }
}
