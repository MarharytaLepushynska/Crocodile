package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

// GET /avatars         — список доступних аватарів (назви без розширення)
// GET /avatars/{name}  — отримати PNG файл аватара
public class AvatarHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!RouterUtil.methodIs(ex, "GET")) {
                RouterUtil.sendError(ex, 405, "Method Not Allowed");
                return;
            }

            String[] segs = RouterUtil.pathSegments(ex);
            if (segs.length == 1) {
                listAvatars(ex);
            } else if (segs.length == 2) {
                serveAvatar(ex, segs[1]);
            } else {
                RouterUtil.sendError(ex, 404, "Not Found");
            }
        } catch (Exception e) {
            RouterUtil.sendError(ex, 500, e.getMessage());
        }
    }

    // GET /avatars — повертає ["cat","dog","fox",...]
    private void listAvatars(HttpExchange ex) throws IOException {
        List<String> names = new ArrayList<>();
        try (InputStream index = getClass().getClassLoader()
                .getResourceAsStream("avatars")) {
            String avatarsPath = getClass().getClassLoader()
                    .getResource("avatars") != null
                    ? getClass().getClassLoader().getResource("avatars").getPath()
                    : null;

            if (avatarsPath != null) {
                java.io.File dir = new java.io.File(avatarsPath);
                if (dir.exists() && dir.isDirectory()) {
                    for (java.io.File f : dir.listFiles()) {
                        String name = f.getName();
                        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                            names.add(name.replaceAll("\\.(png|jpg|jpeg)$", ""));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(names.get(i)).append('"');
        }
        sb.append(']');

        RouterUtil.sendJson(ex, 200, sb.toString());
    }


    private void serveAvatar(HttpExchange ex, String filename) throws IOException {

        if (!filename.contains(".")) filename = filename + ".png";


        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            RouterUtil.sendError(ex, 400, "invalid filename");
            return;
        }

        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("avatars/" + filename);

        if (in == null) {
            RouterUtil.sendError(ex, 404, "avatar not found: " + filename);
            return;
        }

        String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";
        byte[] bytes = in.readAllBytes();
        in.close();

        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
