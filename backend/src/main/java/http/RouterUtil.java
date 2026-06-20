package http;

import com.sun.net.httpserver.HttpExchange;
import utils.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RouterUtil {

    // "/rooms/42/pixels" → ["rooms","42","pixels"]
    public static String[] pathSegments(HttpExchange ex) {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        int start = parts.length > 0 && parts[0].isEmpty() ? 1 : 0;
        String[] result = new String[parts.length - start];
        System.arraycopy(parts, start, result, 0, result.length);
        return result;
    }

    public static long pathId(HttpExchange ex, int segmentIndex) {
        String[] segs = pathSegments(ex);
        if (segmentIndex >= segs.length) throw new IllegalArgumentException("no id in path");
        return Long.parseLong(segs[segmentIndex]);
    }

    public static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> parseBody(HttpExchange ex) throws IOException {
        return JsonUtil.parseObject(readBody(ex));
    }

    public static long getUserId(HttpExchange ex) {
        String header = ex.getRequestHeaders().getFirst("X-User-Id");
        if (header == null || header.isBlank())
            throw new RuntimeException("X-User-Id header missing");
        return Long.parseLong(header.trim());
    }

    public static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendOk(HttpExchange ex, Object body) throws IOException {
        sendJson(ex, 200, JsonUtil.toJson(body));
    }

    public static void sendCreated(HttpExchange ex, Object body) throws IOException {
        sendJson(ex, 201, JsonUtil.toJson(body));
    }

    public static void sendError(HttpExchange ex, int status, String message) throws IOException {
        sendJson(ex, status, "{\"error\":\"" + message.replace("\"", "'") + "\"}");
    }

    public static boolean methodIs(HttpExchange ex, String method) {
        return ex.getRequestMethod().equalsIgnoreCase(method);
    }
}
