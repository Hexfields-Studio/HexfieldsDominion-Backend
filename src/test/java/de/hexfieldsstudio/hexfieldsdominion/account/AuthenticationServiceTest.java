package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.token.CookieService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.RefreshTokensService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.AllUserRepository;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AllUserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private CookieService cookieService;

    @Mock
    private RefreshTokensService refreshTokensService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void testGuest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(jwtService.generateToken(isA(User.class), anyInt())).thenReturn("testToken");
        when(cookieService.createRefreshTokenCookie(isA(User.class))).thenReturn(new Cookie("test", "value"));

        AuthenticationResult authenticationResult = authenticationService.guest();

        this.assertSuccessResponse(authenticationResult);
    }

    @Test
    public void testRegisterSuccess() {
        RegisterDTO registerDTO = new RegisterDTO("testuser", "somePw");

        when(jwtService.generateToken(isA(User.class), anyInt())).thenReturn("testToken");
        when(cookieService.createRefreshTokenCookie(isA(User.class))).thenReturn(new Cookie("test", "value"));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPw");

        AuthenticationResult authenticationResult = authenticationService.register(registerDTO);

        this.assertSuccessResponse(authenticationResult);
    }

    @ParameterizedTest
    @CsvSource({
            "inv<lidName,validPw123",
            "validName,inv>lidPw",
            "inv<lidName,inv>lidPw"
    })
    public void testRegisterFail(String username, String password) {
        RegisterDTO registerDTO = new RegisterDTO(username, password);

        AuthenticationResult authenticationResult = authenticationService.register(registerDTO);
        AuthenticationResponse response = authenticationResult.authenticationResponse();
        Cookie refreshCookie = authenticationResult.refreshTokenCookie();

        assertInstanceOf(ErrorAuthenticationResponse.class, response);
        assertEquals("Invalid credentials", ((ErrorAuthenticationResponse) response).errorMessage());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ((ErrorAuthenticationResponse) response).statusCode());
        assertNull(refreshCookie);
    }

    @Test
    public void testLoginSuccess() {
        String username = "testuser";
        String password = "somePw";

        when(passwordEncoder.encode(password)).thenReturn(password);

        User user = User.builder()
                .username(username)
                .password(password, passwordEncoder)
                .build();

        LoginDTO loginDTO = new LoginDTO(username, password);

        when(jwtService.generateToken(isA(User.class), anyInt())).thenReturn("testToken");
        when(cookieService.createRefreshTokenCookie(isA(User.class))).thenReturn(new Cookie("test", "value"));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AuthenticationResult authenticationResult = authenticationService.login(loginDTO);

        this.assertSuccessResponse(authenticationResult);
    }

    @Test
    public void testLoginFailUnknownUser() {
        LoginDTO loginDTO = new LoginDTO("testuser", "testpw");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authenticationService.login(loginDTO));
    }

    @Test
    public void testLoginFailInvalidPassword() {
        String username = "testuser";
        String password = "somePw";

        when(passwordEncoder.encode(password)).thenReturn(password);

        User user = User.builder()
                .username(username)
                .password(password, passwordEncoder)
                .build();

        LoginDTO loginDTO = new LoginDTO(username, "otherPw");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        AuthenticationResult authenticationResult = authenticationService.login(loginDTO);
        AuthenticationResponse response = authenticationResult.authenticationResponse();
        Cookie refreshCookie = authenticationResult.refreshTokenCookie();

        assertInstanceOf(ErrorAuthenticationResponse.class, response);
        assertEquals("Invalid credentials", ((ErrorAuthenticationResponse) response).errorMessage());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, ((ErrorAuthenticationResponse) response).statusCode());
        assertNull(refreshCookie);
    }

    @Test
    public void testRefreshSuccess() {
        User user = User.builder()
                .username("testuser")
                .build();

        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(refreshTokensService.isValid(isA(User.class), anyString())).thenReturn(true);
        when(jwtService.generateToken(isA(User.class), anyInt())).thenReturn("testToken");
        when(cookieService.createRefreshTokenCookie(isA(User.class))).thenReturn(new Cookie("test", "value"));

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh("someToken");

        assertTrue(resultOptional.isPresent());
        this.assertSuccessResponse(resultOptional.get());
    }

    @Test
    public void testRefreshFailInvalidTokenJwt() {
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh("someToken");

        assertFalse(resultOptional.isPresent());
    }

    @Test
    public void testRefreshFailUnknownUser() {
        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authenticationService.refresh("someToken"));
    }

    @Test
    public void testRefreshFailInvalidTokenForUser() {
        User user = User.builder()
                .username("testuser")
                .build();

        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(refreshTokensService.isValid(isA(User.class), anyString())).thenReturn(false);

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh("someToken");

        assertFalse(resultOptional.isPresent());
    }

    @Test
    public void testLogoutNoOldToken() {
        Cookie deleteTokenCookie = new Cookie("token", "");

        when(cookieService.createDeleteRefreshTokenCookie()).thenReturn(deleteTokenCookie);

        AuthenticationResult authenticationResult = authenticationService.logout(null);

        assertEquals(deleteTokenCookie, authenticationResult.refreshTokenCookie());
        assertNull(authenticationResult.authenticationResponse());
    }

    @Test
    public void testLogoutWithOldToken() {
        Cookie deleteTokenCookie = new Cookie("token", "");

        User user = User.builder()
                .username("testuser")
                .build();

        when(cookieService.createDeleteRefreshTokenCookie()).thenReturn(deleteTokenCookie);
        when(jwtService.extractUsername(anyString())).thenReturn(user.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        AuthenticationResult authenticationResult = authenticationService.logout("oldToken");

        assertEquals(deleteTokenCookie, authenticationResult.refreshTokenCookie());
        assertNull(authenticationResult.authenticationResponse());
    }

    @Test
    public void testLogoutFailUnknownUser() {
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authenticationService.logout("oldToken"));
    }

    private void assertSuccessResponse(AuthenticationResult authenticationResult) {
        AuthenticationResponse response = authenticationResult.authenticationResponse();
        Cookie refreshCookie = authenticationResult.refreshTokenCookie();

        assertInstanceOf(SuccessAuthenticationResponse.class, response);
        assertNotNull(((SuccessAuthenticationResponse) response).accessToken());
        assertNotNull(refreshCookie);
    }

}
