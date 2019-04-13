package testsystem.security;

import com.auth0.jwt.JWT;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static testsystem.security.SecurityConstants.*;

class JWTFactory {
    static String create(String username) {
        return TOKEN_PREFIX + JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(SECRET.getBytes()));
    }
}
