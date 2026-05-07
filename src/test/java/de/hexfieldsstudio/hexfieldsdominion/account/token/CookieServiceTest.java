package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CookieServiceTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CookieService cookieService;

    @Test
    public void testCreateRefreshCookie() {
        User user = User.builder()
                .username("testuser")
                .build();

        String mockToken = "mockToken";

        when(jwtService.generateToken(isA(User.class), isA(int.class))).thenReturn(mockToken);

        Cookie cookie = cookieService.createRefreshTokenCookie(user);

        assertEquals(AuthTokens.REFRESH_TOKEN_NAME, cookie.getName());
        assertEquals(mockToken, cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("None", cookie.getAttribute("sameSite"));
        assertEquals(AuthTokens.REFRESH_TOKEN_MAX_AGE, cookie.getMaxAge());
    }

    @Test
    public void testCreateDeleteRefreshCookie() {
        Cookie cookie = cookieService.createDeleteRefreshTokenCookie();

        assertEquals(AuthTokens.REFRESH_TOKEN_NAME, cookie.getName());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
    }

}
