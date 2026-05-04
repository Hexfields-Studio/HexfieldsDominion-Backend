package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens;
import de.hexfieldsstudio.hexfieldsdominion.account.token.CookieService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.RefreshTokensService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthenticationServiceIT {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokensService refreshTokensService;

    @Autowired
    private AllUserRepository allUserRepository;

    @Autowired
    private GuestUserRepository guestUserRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CookieService cookieService;

    @BeforeEach
    public void setupEach() {
        allUserRepository.deleteAll();
    }

    @Test
    public void testGuest() {
        AuthenticationResult result = authenticationService.guest();

        SuccessAuthenticationResponse successResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(successResponse.accessToken());
        String usernameRefreshToken = jwtService.extractUsername(result.refreshTokenCookie().getValue());

        assertEquals(usernameAccessToken, usernameRefreshToken);
        assertTrue(usernameAccessToken.startsWith("Guest_"));
        assertTrue(allUserRepository.findByUsername(usernameAccessToken).isPresent());
        assertTrue(guestUserRepository.findByUsername(usernameAccessToken).isPresent());
        assertFalse(accountUserRepository.findByUsername(usernameAccessToken).isPresent());
    }

    @Test
    public void testRegisterSuccess() {
        RegisterDTO registerDTO = new RegisterDTO("testuser", "somePw");

        AuthenticationResult result = authenticationService.register(registerDTO);

        SuccessAuthenticationResponse successResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(successResponse.accessToken());
        String usernameRefreshToken = jwtService.extractUsername(result.refreshTokenCookie().getValue());

        assertEquals(usernameAccessToken, usernameRefreshToken);
        assertEquals(registerDTO.username(), usernameAccessToken);
        assertTrue(allUserRepository.findByUsername(usernameAccessToken).isPresent());
        assertTrue(accountUserRepository.findByUsername(usernameAccessToken).isPresent());
        assertFalse(guestUserRepository.findByUsername(usernameAccessToken).isPresent());

        String storedPassword = allUserRepository.findByUsername(usernameAccessToken).get().getPassword();
        assertTrue(passwordEncoder.matches(registerDTO.password(), storedPassword));
    }

    @ParameterizedTest
    @CsvSource({
            "inv<lidName,validPw123",
            "validName,inv>lidPw",
            "inv<lidName,inv>lidPw"
    })
    public void testRegisterFailInvalidCredentials(String username, String password) {
        RegisterDTO registerDTO = new RegisterDTO(username, password);

        AuthenticationResult result = authenticationService.register(registerDTO);

        ErrorAuthenticationResponse errorResponse = this.assertErrorResponse(result);

        assertEquals("Invalid credentials", errorResponse.errorMessage());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, errorResponse.statusCode());

        assertFalse(allUserRepository.findByUsername(username).isPresent());
    }

    @Test
    public void testLoginSuccess() {
        String username = "testuser";
        String password = "somePw";

        User user = User.builder()
                .username(username)
                .password(password, passwordEncoder)
                .role(Role.PLAYER)
                .build();

        allUserRepository.save(user);

        LoginDTO loginDTO = new LoginDTO(username, password);

        AuthenticationResult result = authenticationService.login(loginDTO);

        SuccessAuthenticationResponse successResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(successResponse.accessToken());
        String usernameRefreshToken = jwtService.extractUsername(result.refreshTokenCookie().getValue());

        assertEquals(usernameAccessToken, usernameRefreshToken);
        assertEquals(username, usernameAccessToken);
    }

    @Test
    public void testLoginFailUnknownUser() {
        LoginDTO loginDTO = new LoginDTO("testuser", "testpw");

        assertThrows(NoSuchElementException.class, () -> authenticationService.login(loginDTO));
    }

    @Test
    public void testLoginFailInvalidPassword() {
        String username = "testuser";
        String password = "somePw";

        User user = User.builder()
                .username(username)
                .password(password, passwordEncoder)
                .role(Role.PLAYER)
                .build();

        allUserRepository.save(user);

        LoginDTO loginDTO = new LoginDTO(username, "otherPw");

        AuthenticationResult result = authenticationService.login(loginDTO);

        ErrorAuthenticationResponse errorResponse = this.assertErrorResponse(result);

        assertEquals("Invalid credentials", errorResponse.errorMessage());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, errorResponse.statusCode());
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    public void testRefreshSuccess(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();
        user.setRefreshToken(refreshToken, passwordEncoder);

        allUserRepository.save(user);

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh(refreshToken);

        assertTrue(resultOptional.isPresent());
        AuthenticationResult result = resultOptional.get();

        SuccessAuthenticationResponse successResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(successResponse.accessToken());
        String usernameRefreshToken = jwtService.extractUsername(result.refreshTokenCookie().getValue());

        assertEquals(usernameAccessToken, usernameRefreshToken);
        assertEquals(user.getUsername(), usernameAccessToken);
    }

    @Test
    public void testRefreshFailInvalidTokenJwt() {
        Optional<AuthenticationResult> resultOptional = authenticationService.refresh("noJwt");

        assertFalse(resultOptional.isPresent());
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    public void testRefreshFailUnknownUser(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();

        assertThrows(NoSuchElementException.class, () -> authenticationService.refresh(refreshToken));
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    public void testRefreshFailInvalidTokenForUser(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();

        user.setRefreshToken(refreshToken, passwordEncoder);
        allUserRepository.save(user);

        refreshTokensService.invalidate(user, allUserRepository);

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh(refreshToken);

        assertFalse(resultOptional.isPresent());
    }

    @Test
    public void testLogoutNoOldToken() {
        AuthenticationResult authenticationResult = authenticationService.logout(null);

        this.assertLogoutResultSuccess(authenticationResult);
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    public void testLogoutWithOldToken(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();

        user.setRefreshToken(refreshToken, passwordEncoder);
        allUserRepository.save(user);

        AuthenticationResult authenticationResult = authenticationService.logout(refreshToken);

        this.assertLogoutResultSuccess(authenticationResult);
        assertTrue(allUserRepository.findByUsername(user.getUsername()).isPresent());
        assertNull(allUserRepository.findByUsername(user.getUsername()).get().getRefreshToken());
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    public void testLogoutFailUnknownUser(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();

        assertThrows(NoSuchElementException.class, () -> authenticationService.logout(refreshToken));
    }

    private SuccessAuthenticationResponse assertSuccessResponse(AuthenticationResult result) {
        assertNotNull(result);
        assertNotNull(result.authenticationResponse());
        assertNotNull(result.refreshTokenCookie());

        assertTrue(jwtService.isTokenValid(result.refreshTokenCookie().getValue()));
        assertEquals(AuthTokens.REFRESH_TOKEN_NAME, result.refreshTokenCookie().getName());
        assertEquals(AuthTokens.REFRESH_TOKEN_MAX_AGE, result.refreshTokenCookie().getMaxAge());

        assertInstanceOf(SuccessAuthenticationResponse.class, result.authenticationResponse());
        SuccessAuthenticationResponse successResponse = (SuccessAuthenticationResponse) result.authenticationResponse();

        assertNotNull(successResponse.accessToken());
        assertTrue(jwtService.isTokenValid(successResponse.accessToken()));

        return successResponse;
    }

    private ErrorAuthenticationResponse assertErrorResponse(AuthenticationResult result) {
        assertNotNull(result);
        assertNotNull(result.authenticationResponse());
        assertNull(result.refreshTokenCookie());

        assertInstanceOf(ErrorAuthenticationResponse.class, result.authenticationResponse());

        return (ErrorAuthenticationResponse) result.authenticationResponse();
    }

    private void assertLogoutResultSuccess(AuthenticationResult authenticationResult) {
        assertNull(authenticationResult.authenticationResponse());

        Cookie refreshCookie = authenticationResult.refreshTokenCookie();
        assertNotNull(refreshCookie);
        assertEquals(AuthTokens.REFRESH_TOKEN_NAME, refreshCookie.getName());
        assertEquals(0, refreshCookie.getMaxAge());
    }

    static class RolesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(Role.GUEST),
                    Arguments.of(Role.PLAYER)
            );
        }
    }

}
