package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.error.InvalidCharactersException;
import de.hexfieldsstudio.hexfieldsdominion.account.error.InvalidCredentialsException;
import de.hexfieldsstudio.hexfieldsdominion.account.error.UserAlreadyExistsException;
import de.hexfieldsstudio.hexfieldsdominion.account.token.CookieService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.RefreshTokensService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.AllUserRepository;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

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
    void testGuest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(jwtService.generateToken(isA(User.class), anyInt())).thenReturn("testToken");
        when(cookieService.createRefreshTokenCookie(isA(User.class))).thenReturn(new Cookie("test", "value"));

        AuthenticationResult authenticationResult = authenticationService.guest();

        this.assertSuccessResponse(authenticationResult);
    }

    @Test
    void testRegisterSuccess() {
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
    void testRegisterFailInvalidCharacters(String username, String password) {
        RegisterDTO registerDTO = new RegisterDTO(username, password);

        assertThrows(InvalidCharactersException.class, () -> authenticationService.register(registerDTO));
    }

    @Test
    void testRegisterFailUserAlreadyExists() {
        String username = "testuser";
        String password = "somePw";

        when(passwordEncoder.encode(password)).thenReturn(password);

        User user = User.builder()
                .username(username)
                .password(password, passwordEncoder)
                .build();

        RegisterDTO registerDTO = new RegisterDTO("testuser", "somePw");

        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(registerDTO));
    }

    @Test
    void testLoginSuccess() {
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
    void testLoginFailUnknownUser() {
        LoginDTO loginDTO = new LoginDTO("testuser", "testpw");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(loginDTO));
    }

    @Test
    void testLoginFailInvalidPassword() {
        String username = "testuser";
        String password = "somePw";

        when(passwordEncoder.encode(password)).thenReturn(password);

        User user = User.builder()
                .username(username)
                .password(password, passwordEncoder)
                .build();

        LoginDTO loginDTO = new LoginDTO(username, "otherPw");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(loginDTO));
    }

    @Test
    void testRefreshSuccess() {
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
    void testRefreshFailInvalidTokenJwt() {
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh("someToken");

        assertFalse(resultOptional.isPresent());
    }

    @Test
    void testRefreshFailUnknownUser() {
        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh("someToken");

        assertFalse(resultOptional.isPresent());
    }

    @Test
    void testRefreshFailInvalidTokenForUser() {
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
    void testLogoutNoOldToken() {
        Cookie deleteTokenCookie = new Cookie("token", "");

        when(cookieService.createDeleteRefreshTokenCookie()).thenReturn(deleteTokenCookie);

        Optional<AuthenticationResult> authenticationResultOptional = authenticationService.logout(null);

        assertTrue(authenticationResultOptional.isPresent());
        AuthenticationResult authenticationResult = authenticationResultOptional.get();
        assertEquals(deleteTokenCookie, authenticationResult.refreshTokenCookie());
        assertNull(authenticationResult.authenticationResponse());
    }

    @Test
    void testLogoutWithOldToken() {
        Cookie deleteTokenCookie = new Cookie("token", "");

        User user = User.builder()
                .username("testuser")
                .build();

        when(cookieService.createDeleteRefreshTokenCookie()).thenReturn(deleteTokenCookie);
        when(jwtService.extractUsername(anyString())).thenReturn(user.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        Optional<AuthenticationResult> authenticationResultOptional = authenticationService.logout("oldToken");

        assertTrue(authenticationResultOptional.isPresent());
        AuthenticationResult authenticationResult = authenticationResultOptional.get();
        assertEquals(deleteTokenCookie, authenticationResult.refreshTokenCookie());
        assertNull(authenticationResult.authenticationResponse());
    }

    @Test
    void testLogoutFailUnknownUser() {
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<AuthenticationResult> authenticationResultOptional = authenticationService.logout("oldToken");

        assertFalse(authenticationResultOptional.isPresent());
    }

    private void assertSuccessResponse(AuthenticationResult authenticationResult) {
        AuthenticationResponse response = authenticationResult.authenticationResponse();
        Cookie refreshCookie = authenticationResult.refreshTokenCookie();

        assertNotNull(response.accessToken());
        assertNotNull(refreshCookie);
    }

}
