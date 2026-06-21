package httpConnection;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import utils.JwtUtil;

import java.util.List;

//клас для запитів не login/register
public class JwtAuthenticator extends Authenticator {

    @Override
    public Result authenticate(HttpExchange exch) {

        //Я бачила, що можливо треба окремо обробити OPTIONS вже при роботу з браузером, але це поки на потім

        List<String> values = exch.getRequestHeaders().get("Authorization");
        if(values == null || values.isEmpty()) {
            return new Failure(401);
        }

        String[] parts = values.getFirst().split(" ");
        if (parts.length != 2 || !parts[0].equals("Bearer")) {
            return new Failure(401);
        }

        String token = parts[1];

        try {
            DecodedJWT decodedJWT = JwtUtil.verifyTocken(token);
            long userId = JwtUtil.extractUserId(decodedJWT);
            String username = JwtUtil.extractUsername(decodedJWT);

            AuthContext.setAuthenticatedUser(exch, userId, username);

            return new Success(new HttpPrincipal(username, "ROLE_PLAYER"));
        } catch (JWTVerificationException e) {
            return new Failure(401);
        }
    }
}
