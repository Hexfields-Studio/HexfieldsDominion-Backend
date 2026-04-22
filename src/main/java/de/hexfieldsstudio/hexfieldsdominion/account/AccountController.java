package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens.REFRESH_TOKEN_NAME;

@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class AccountController {

    private final AuthenticationService authenticationService;

    @PostMapping("/guest")
    public ResponseEntity<AuthenticationResponse> guest(HttpServletResponse response) {
        AuthenticationResult result = authenticationService.guest();

        response.addCookie(result.refreshTokenCookie());

        return ResponseEntity.ok(result.authenticationResponse());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterDTO request, HttpServletResponse response) {
        AuthenticationResult result = authenticationService.register(request);

        if (result.authenticationResponse() instanceof ErrorAuthenticationResponse errorResponse) {
            return ResponseEntity.status(errorResponse.statusCode()).body(errorResponse);
        }

        response.addCookie(result.refreshTokenCookie());

        return ResponseEntity.ok(result.authenticationResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginDTO request, HttpServletResponse response) {
        AuthenticationResult result = authenticationService.login(request);

        response.addCookie(result.refreshTokenCookie());

        return ResponseEntity.ok(result.authenticationResponse());
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@CookieValue(REFRESH_TOKEN_NAME) String oldRefreshToken, HttpServletResponse response) {
        return authenticationService.refresh(oldRefreshToken).map(result -> {
            response.addCookie(result.refreshTokenCookie());

            return ResponseEntity.ok(result.authenticationResponse());

        }).orElse(ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public void logout(@CookieValue(name = REFRESH_TOKEN_NAME, required = false) String oldRefreshToken, HttpServletResponse response) {
        AuthenticationResult result = authenticationService.logout(oldRefreshToken);

        response.addCookie(result.refreshTokenCookie());

        response.setStatus(HttpServletResponse.SC_OK);
    }

}