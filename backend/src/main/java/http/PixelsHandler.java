package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import packet.PixelBatch;
import utils.DataBase;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// PUT /rooms/{id}/pixels  — художник надсилає пікселі  (X-User-Id = drawer)
//   body: plain-text у форматі PixelBatch: "x,y,color;x,y,color;..."
// GET /rooms/{id}/pixels  — всі гравці отримують поточний canvas
public class PixelsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            // segs: ["rooms", "{id}", "pixels"]
            String[] segs = RouterUtil.pathSegments(ex);
            if (segs.length < 2) { RouterUtil.sendError(ex, 400, "missing room id"); return; }
            long roomId = Long.parseLong(segs[1]);

            if (RouterUtil.methodIs(ex, "PUT")) {
                submitPixels(ex, roomId);
            } else if (RouterUtil.methodIs(ex, "GET")) {
                getCanvas(ex, roomId);
            } else {
                RouterUtil.sendError(ex, 405, "Method Not Allowed");
            }
        } catch (NumberFormatException e) {
            RouterUtil.sendError(ex, 400, "invalid room id");
        } catch (Exception e) {
            RouterUtil.sendError(ex, 400, e.getMessage());
        }
    }

    private void submitPixels(HttpExchange ex, long roomId) throws IOException {
        long userId = RouterUtil.getUserId(ex);
        String raw = RouterUtil.readBody(ex);
        List<PixelBatch.Pixel> pixels = PixelBatch.decode(raw);
        DataBase.getGameService().submitPixels(roomId, userId, pixels);
        RouterUtil.sendOk(ex, Map.of("accepted", pixels.size()));
    }

    private void getCanvas(HttpExchange ex, long roomId) throws IOException {
        // перевіряємо таймаут раунду
        DataBase.getGameService().checkRoundTimeout(roomId);

        List<PixelBatch.Pixel> canvas = DataBase.getGameService().getSession(roomId).getCanvas();
        String encoded = PixelBatch.encode(canvas);
        RouterUtil.sendOk(ex, Map.of("pixels", encoded, "count", canvas.size()));
    }
}
