package httpConnection;

import com.sun.net.httpserver.HttpExchange;

//доп клас щоб можна було зберегти інформацію про юзера
public class AuthContext {
    private static final String USER_ID = "auth.userId";
    private static final String USERNAME = "auth.username";

    private AuthContext() {

    }

    public static void setAuthenticatedUser(HttpExchange ex, long userId, String username) {
        ex.setAttribute(USER_ID, userId);
        ex.setAttribute(USERNAME, username);
    }

    //TODO: придумати влучнішу помилку
    public static long getUserId(HttpExchange ex) {
        Object value = ex.getAttribute(USER_ID);
        if (value == null) {
            throw new IllegalStateException("No user id found");
        }
        return (long) value;
    }

    public static String getUsername(HttpExchange ex) {
        Object value = ex.getAttribute(USERNAME);
        if (value == null) {
            throw new IllegalStateException("No username found");
        }
        return (String) value;
    }
}
