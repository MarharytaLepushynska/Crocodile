package utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

//для перевірки токенів
public class JwtUtil {
    private static final String SECRET = resolveSecret();
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final long EXPIRATION = 24L * 120000;

    private static String resolveSecret() {
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.isBlank()) {
            return envSecret;
        }

        return "crocodile-secret";
    }

    public static String generateToken(long userId, String username) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION))
                .sign(ALGORITHM);
    }

    public static DecodedJWT verifyTocken(String tocken) throws JWTVerificationException {
        return JWT.require(ALGORITHM).build().verify(tocken);
    }

    public static long extractUserId(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("userId").asLong();
    }

    public static String extractUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("username").asString();
    }
}
