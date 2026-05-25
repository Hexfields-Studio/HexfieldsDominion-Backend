package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.token.SseTokenService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens.REFRESH_TOKEN_NAME;

@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class AccountController {

    private final AuthenticationService authenticationService;
    private final SseTokenService sseTokenService;

    @PostMapping("/guest")
    public ResponseEntity<@NonNull AuthenticationResponse> guest(HttpServletResponse response) {
        AuthenticationResult result = authenticationService.guest();

        response.addCookie(result.refreshTokenCookie());

        return ResponseEntity.ok(result.authenticationResponse());
    }

    @PostMapping("/register")
    public ResponseEntity<@NonNull AuthenticationResponse> register(@RequestBody RegisterDTO request, HttpServletResponse response) {
        AuthenticationResult result = authenticationService.register(request);

        response.addCookie(result.refreshTokenCookie());

        return ResponseEntity.ok(result.authenticationResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<@NonNull AuthenticationResponse> login(@RequestBody LoginDTO request, HttpServletResponse response) {
        AuthenticationResult result = authenticationService.login(request);

        response.addCookie(result.refreshTokenCookie());

        return ResponseEntity.ok(result.authenticationResponse());
    }

    @GetMapping("/refresh")
    public ResponseEntity<@NonNull AuthenticationResponse> refresh(@CookieValue(REFRESH_TOKEN_NAME) String oldRefreshToken, HttpServletResponse response) {
        return authenticationService.refresh(oldRefreshToken).map(result -> {
            response.addCookie(result.refreshTokenCookie());

            return ResponseEntity.ok(result.authenticationResponse());

        }).orElse(ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public void logout(@CookieValue(name = REFRESH_TOKEN_NAME, required = false) String oldRefreshToken, HttpServletResponse response) {
        Optional<AuthenticationResult> resultOptional = authenticationService.logout(oldRefreshToken);
        if (resultOptional.isEmpty()) {
            return;
        }

        response.addCookie(resultOptional.get().refreshTokenCookie());

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @GetMapping("/ssetoken")
    public ResponseEntity<@NonNull String> sseToken() {
        User user = AuthUtils.getAuthenticatedUser();

        String token = sseTokenService.createToken(user);

        return ResponseEntity.ok(token);
    }

}