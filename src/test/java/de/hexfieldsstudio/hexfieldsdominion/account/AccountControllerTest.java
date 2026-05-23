package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.token.SseTokenService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @InjectMocks
    private AccountController accountController;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private SseTokenService sseTokenService;

    @Mock
    private HttpServletResponse response;

    @Test
    void testGuest() {
        this.testAuthSuccess(authenticationResult -> {
            when(authenticationService.guest()).thenReturn(authenticationResult);

            return accountController.guest(response);
        });
    }

    @Test
    void testRegister() {
        this.testAuthSuccess(authenticationResult -> {
            RegisterDTO registerDTO = new RegisterDTO("testuser", "pw");

            when(authenticationService.register(registerDTO)).thenReturn(authenticationResult);

            return accountController.register(registerDTO, response);
        });
    }

    @Test
    void testLogin() {
        this.testAuthSuccess(authenticationResult -> {
            LoginDTO loginDTO = new LoginDTO("testuser", "pw");

            when(authenticationService.login(loginDTO)).thenReturn(authenticationResult);

            return accountController.login(loginDTO, response);
        });
    }

    @Test
    void testRefreshSuccess() {
        this.testAuthSuccess(authenticationResult -> {
            when(authenticationService.refresh(anyString())).thenReturn(Optional.of(authenticationResult));

            return accountController.refresh("oldToken", response);
        });
    }

    @Test
    void testRefreshFailInvalidToken() {
        when(authenticationService.refresh(anyString())).thenReturn(Optional.empty());

        ResponseEntity<@NonNull AuthenticationResponse> responseEntity = accountController.refresh("oldToken", response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, responseEntity.getStatusCode().value());
        verify(response, never()).addCookie(isA(Cookie.class));
    }

    @Test
    void testLogout() {
        AuthenticationResult authenticationResult = AuthenticationResult.builder()
                .refreshTokenCookie(new Cookie("logoutCookie", ""))
                .build();

        when(authenticationService.logout(any())).thenReturn(Optional.of(authenticationResult));
        accountController.logout(null, response);

        verify(response).addCookie(authenticationResult.refreshTokenCookie());
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void testLogoutUnknownUser() {
        AuthenticationResult authenticationResult = AuthenticationResult.builder()
                .refreshTokenCookie(new Cookie("logoutCookie", ""))
                .build();

        when(authenticationService.logout(any())).thenReturn(Optional.empty());
        accountController.logout(null, response);

        verify(response, never()).addCookie(authenticationResult.refreshTokenCookie());
    }

    @Test
    void testSseToken() {
        String createdToken = "someToken";

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(new User());
            when(sseTokenService.createToken(any())).thenReturn(createdToken);

            ResponseEntity<@NonNull String> response = accountController.sseToken();

            assertEquals(HttpServletResponse.SC_OK, response.getStatusCode().value());
            assertEquals(createdToken, response.getBody());
        }
    }

    private void testAuthSuccess(Function<AuthenticationResult, ResponseEntity<@NonNull AuthenticationResponse>> function) {
        AuthenticationResult authenticationResult = AuthenticationResult.builder()
                .authenticationResponse(new AuthenticationResponse("token"))
                .refreshTokenCookie(new Cookie("tokenCookie", "token"))
                .build();

        ResponseEntity<@NonNull AuthenticationResponse> responseEntity = function.apply(authenticationResult);

        assertEquals(HttpServletResponse.SC_OK, responseEntity.getStatusCode().value());
        assertEquals(authenticationResult.authenticationResponse(), responseEntity.getBody());
        // we can't test if the responseEntity or response contains the cookie (no getter)
        verify(response).addCookie(authenticationResult.refreshTokenCookie());
    }


}
