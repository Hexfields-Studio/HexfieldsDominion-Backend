package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.error.InvalidCharactersException;
import de.hexfieldsstudio.hexfieldsdominion.account.error.InvalidCredentialsException;
import de.hexfieldsstudio.hexfieldsdominion.account.error.UserAlreadyExistsException;
import de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens;
import de.hexfieldsstudio.hexfieldsdominion.account.token.CookieService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.RefreshTokensService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.*;
import jakarta.servlet.http.Cookie;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthenticationServiceIT {

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
    void setupEach() {
        allUserRepository.deleteAll();
    }

    @Test
    void testGuest() {
        AuthenticationResult result = authenticationService.guest();

        AuthenticationResponse successResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(successResponse.accessToken());
        String usernameRefreshToken = jwtService.extractUsername(result.refreshTokenCookie().getValue());

        assertEquals(usernameAccessToken, usernameRefreshToken);
        assertTrue(usernameAccessToken.startsWith("Guest_"));
        assertTrue(allUserRepository.findByUsername(usernameAccessToken).isPresent());
        assertTrue(guestUserRepository.findByUsername(usernameAccessToken).isPresent());
        assertFalse(accountUserRepository.findByUsername(usernameAccessToken).isPresent());
    }

    @Test
    void testRegisterSuccess() {
        RegisterDTO registerDTO = new RegisterDTO("testuser", "somePw");

        AuthenticationResult result = authenticationService.register(registerDTO);

        AuthenticationResponse authenticationResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(authenticationResponse.accessToken());
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
    void testRegisterFailInvalidCredentials(String username, String password) {
        RegisterDTO registerDTO = new RegisterDTO(username, password);

        assertThrows(InvalidCharactersException.class, () -> authenticationService.register(registerDTO));

        assertFalse(allUserRepository.findByUsername(username).isPresent());
    }

    @Test
    void testRegisterFailUserAlreadyExists() {
        RegisterDTO registerDTO = new RegisterDTO("testuser", "somePw");
        authenticationService.register(registerDTO);

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(registerDTO));
    }

    @Test
    void testLoginSuccess() {
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

        AuthenticationResponse authenticationResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(authenticationResponse.accessToken());
        String usernameRefreshToken = jwtService.extractUsername(result.refreshTokenCookie().getValue());

        assertEquals(usernameAccessToken, usernameRefreshToken);
        assertEquals(username, usernameAccessToken);
    }

    @Test
    void testLoginFailUnknownUser() {
        LoginDTO loginDTO = new LoginDTO("testuser", "testpw");

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(loginDTO));
    }

    @Test
    void testLoginFailInvalidPassword() {
        String username = "testuser";
        String password = "somePw";

        User user = User.builder()
                .username(username)
                .password(password, passwordEncoder)
                .role(Role.PLAYER)
                .build();

        allUserRepository.save(user);

        LoginDTO loginDTO = new LoginDTO(username, "otherPw");

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(loginDTO));
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    void testRefreshSuccess(Role role) {
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

        AuthenticationResponse authenticationResponse = this.assertSuccessResponse(result);

        String usernameAccessToken = jwtService.extractUsername(authenticationResponse.accessToken());
        String usernameRefreshToken = jwtService.extractUsername(result.refreshTokenCookie().getValue());

        assertEquals(usernameAccessToken, usernameRefreshToken);
        assertEquals(user.getUsername(), usernameAccessToken);
    }

    @Test
    void testRefreshFailInvalidTokenJwt() {
        Optional<AuthenticationResult> resultOptional = authenticationService.refresh("noJwt");

        assertFalse(resultOptional.isPresent());
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    void testRefreshFailUnknownUser(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();

        Optional<AuthenticationResult> resultOptional = authenticationService.refresh(refreshToken);

        assertFalse(resultOptional.isPresent());
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    void testRefreshFailInvalidTokenForUser(Role role) {
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
    void testLogoutNoOldToken() {
        Optional<AuthenticationResult> authenticationResultOptional = authenticationService.logout(null);

        assertTrue(authenticationResultOptional.isPresent());
        this.assertLogoutResultSuccess(authenticationResultOptional.get());
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    void testLogoutWithOldToken(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();

        user.setRefreshToken(refreshToken, passwordEncoder);
        allUserRepository.save(user);

        Optional<AuthenticationResult> authenticationResultOptional = authenticationService.logout(refreshToken);

        assertTrue(authenticationResultOptional.isPresent());
        this.assertLogoutResultSuccess(authenticationResultOptional.get());
        assertTrue(allUserRepository.findByUsername(user.getUsername()).isPresent());
        assertNull(allUserRepository.findByUsername(user.getUsername()).get().getRefreshToken());
    }

    @ParameterizedTest
    @ArgumentsSource(RolesProvider.class)
    void testLogoutFailUnknownUser(Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        String refreshToken = cookieService.createRefreshTokenCookie(user).getValue();

        Optional<AuthenticationResult> authenticationResultOptional = authenticationService.logout(refreshToken);

        assertFalse(authenticationResultOptional.isPresent());
    }

    private AuthenticationResponse assertSuccessResponse(AuthenticationResult result) {
        assertNotNull(result);
        assertNotNull(result.authenticationResponse());
        assertNotNull(result.refreshTokenCookie());

        assertTrue(jwtService.isTokenValid(result.refreshTokenCookie().getValue()));
        assertEquals(AuthTokens.REFRESH_TOKEN_NAME, result.refreshTokenCookie().getName());
        assertEquals(AuthTokens.REFRESH_TOKEN_MAX_AGE, result.refreshTokenCookie().getMaxAge());

        AuthenticationResponse authenticationResponse = result.authenticationResponse();

        assertNotNull(authenticationResponse.accessToken());
        assertTrue(jwtService.isTokenValid(authenticationResponse.accessToken()));

        return authenticationResponse;
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
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(Role.GUEST),
                    Arguments.of(Role.PLAYER)
            );
        }
    }

}
