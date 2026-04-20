package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.token.ValidRefreshTokensService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.AllUserRepository;
import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.account.token.CookieService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens.ACCESS_TOKEN_MAX_AGE;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Pattern VALID_USERNAME_PW_PATTERN = Pattern.compile("^([a-zA-Z0-9*._\\-+=()!%@])+$");

    private final AllUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final ValidRefreshTokensService validRefreshTokensService;

    public AuthenticationResult guest() {
        String guestUsername;
        do {
            guestUsername = "Guest_%s".formatted(UUID.randomUUID().toString());
        } while (userRepository.findByUsername((guestUsername)).isPresent());

        User user = User.builder()
                .username(guestUsername)
                .role(Role.GUEST)
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user, ACCESS_TOKEN_MAX_AGE);
        AuthenticationResponse response = new SuccessAuthenticationResponse(accessToken);

        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(user);
        validRefreshTokensService.store(user, refreshTokenCookie.getValue());

        return AuthenticationResult.builder()
                .authenticationResponse(response)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

    public AuthenticationResult register(RegisterDTO request) {
        if (!VALID_USERNAME_PW_PATTERN.matcher(request.getUsername()).matches() || !VALID_USERNAME_PW_PATTERN.matcher(request.getPassword()).matches()) {
            return AuthenticationResult.builder()
                    .authenticationResponse(new ErrorAuthenticationResponse("Invalid credentials"))
                    .build();
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PLAYER)
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user, ACCESS_TOKEN_MAX_AGE);
        AuthenticationResponse response = new SuccessAuthenticationResponse(accessToken);

        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(user);
        validRefreshTokensService.store(user, refreshTokenCookie.getValue());

        return AuthenticationResult.builder()
                .authenticationResponse(response)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

    public AuthenticationResult login(LoginDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String accessToken = jwtService.generateToken(user, ACCESS_TOKEN_MAX_AGE);
        AuthenticationResponse response = new SuccessAuthenticationResponse(accessToken);

        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(user);
        validRefreshTokensService.store(user, refreshTokenCookie.getValue());

        return AuthenticationResult.builder()
                .authenticationResponse(response)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

    public Optional<AuthenticationResult> refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken) || !validRefreshTokensService.isValid(refreshToken)) {
            return Optional.empty();
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        String accessToken = jwtService.generateToken(user, ACCESS_TOKEN_MAX_AGE);
        AuthenticationResponse response = new SuccessAuthenticationResponse(accessToken);

        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(user);
        // no need to invalidate the old token as this replaces it with the new one
        validRefreshTokensService.store(user, refreshTokenCookie.getValue());

        return Optional.of(AuthenticationResult.builder()
                .authenticationResponse(response)
                .refreshTokenCookie(refreshTokenCookie)
                .build());
    }

    public AuthenticationResult logout(String oldRefreshToken) {
        if (oldRefreshToken != null) {
            String username = jwtService.extractUsername(oldRefreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow();

            validRefreshTokensService.invalidate(user);
        }

        return AuthenticationResult.builder()
                .refreshTokenCookie(cookieService.createDeleteRefreshTokenCookie())
                .build();
    }

}
