package utils;

import packet.PixelBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    public static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + escape(s) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(toJson(list.get(i)));
            }
            return sb.append(']').toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey().toString())).append("\":");
                sb.append(toJson(e.getValue()));
            }
            return sb.append('}').toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    // Парсить плаский JSON-об'єкт (без вкладень): {"key":"value","num":42}
    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null) return result;
        String s = json.strip();
        if (!s.startsWith("{")) return result;
        s = s.substring(1, s.endsWith("}") ? s.length() - 1 : s.length()).strip();
        if (s.isEmpty()) return result;

        int i = 0;
        while (i < s.length()) {
            // читаємо ключ
            if (s.charAt(i) != '"') { i++; continue; }
            int keyStart = i + 1;
            int keyEnd = s.indexOf('"', keyStart);
            if (keyEnd < 0) break;
            String key = s.substring(keyStart, keyEnd);
            i = keyEnd + 1;

            // шукаємо ':'
            while (i < s.length() && s.charAt(i) != ':') i++;
            i++; // пропускаємо ':'
            while (i < s.length() && s.charAt(i) == ' ') i++;

            // читаємо значення
            String val;
            if (i < s.length() && s.charAt(i) == '"') {
                // рядкове значення
                int valStart = i + 1;
                int valEnd = valStart;
                while (valEnd < s.length()) {
                    if (s.charAt(valEnd) == '\\') { valEnd += 2; continue; }
                    if (s.charAt(valEnd) == '"') break;
                    valEnd++;
                }
                val = s.substring(valStart, valEnd);
                i = valEnd + 1;
            } else {
                // числове/булеве/null
                int valEnd = i;
                while (valEnd < s.length() && s.charAt(valEnd) != ',' && s.charAt(valEnd) != '}') valEnd++;
                val = s.substring(i, valEnd).strip();
                i = valEnd;
            }
            result.put(key, unescape(val));
            // пропускаємо кому
            while (i < s.length() && (s.charAt(i) == ',' || s.charAt(i) == ' ')) i++;
        }
        return result;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    public static List<Map<String, String>> parseArrayOfObjects(String json, String key) {
        List<Map<String, String>> result = new ArrayList<>();
        if (json == null) return result;

        String s = json.strip();
        if (!s.startsWith("{")) return result;
        s = s.substring(1, s.endsWith("}") ? s.length() - 1 : s.length()).strip();

        String marker = "\"" + key + "\"";
        int keyIndex = s.indexOf(marker);
        if (keyIndex < 0) return result;

        int arrayStart = s.indexOf('[', keyIndex);
        if (arrayStart < 0) return result;

        int arrayEnd = findMatchingBracket(s, arrayStart, '[', ']');
        if (arrayEnd < 0) return result;

        String arrayContent = s.substring(arrayStart + 1, arrayEnd).strip();
        if (arrayContent.isEmpty()) return result;

        int i = 0;
        while (i < arrayContent.length()) {
            if (arrayContent.charAt(i) != '{') { i++; continue; }

            int objEnd = findMatchingBracket(arrayContent, i, '{', '}');
            if (objEnd < 0) break;

            String objectJson = arrayContent.substring(i, objEnd + 1);
            result.add(parseObject(objectJson));

            i = objEnd + 1;
        }

        return result;
    }

    private static int findMatchingBracket(String s, int openIndex, char open, char close) {
        int depth = 0;
        for( int i = openIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }
}
