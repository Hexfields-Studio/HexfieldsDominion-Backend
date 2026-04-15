package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens.REFRESH_TOKEN_MAX_AGE;
import static de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens.REFRESH_TOKEN_NAME;

@Service
@AllArgsConstructor
public class CookieService {

    private final JwtService jwtService;

    public Cookie createRefreshTokenCookie(User user) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_NAME, jwtService.generateToken(user, REFRESH_TOKEN_MAX_AGE));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("sameSite", "Lax");
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        return cookie;
    }

    public Cookie createDeleteRefreshTokenCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

}
