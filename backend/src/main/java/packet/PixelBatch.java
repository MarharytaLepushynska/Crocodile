package packet;

import java.util.ArrayList;
import java.util.List;

//якщо що це клас для серіалізації/десеріалізації пакету пікселів, який передається між клієнтом і сервером
//у форматі "x,y,colorIndex;x,y,colorIndex;..."
public class PixelBatch {

    public static class Pixel {
        private final int x;
        private final int y;
        private final int colorIndex;

        public Pixel(int x, int y, int colorIndex) {
            this.x = x;
            this.y = y;
            this.colorIndex = colorIndex;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getColorIndex() {
            return colorIndex;
        }
    }

    // формат: "x,y,colorIndex;x,y,colorIndex;..."
    public static String encode(List<Pixel> pixels) {
        StringBuilder sb = new StringBuilder();
        for (Pixel p : pixels) {
            sb.append(p.getX()).append(',')
                    .append(p.getY()).append(',')
                    .append(p.getColorIndex()).append(';');
        }
        if (sb.length() > 0)
            sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    public static List<Pixel> decode(String message) {
        List<Pixel> result = new ArrayList<>();
        if (message == null || message.isEmpty())
            return result;

        for (String entry : message.split(";")) {
            if (entry.isBlank())
                continue;

            String[] parts = entry.split(",");
            result.add(new Pixel(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            ));
        }

        return result;
    }
}