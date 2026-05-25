package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.error.InvalidCharactersException;
import de.hexfieldsstudio.hexfieldsdominion.account.error.InvalidCredentialsException;
import de.hexfieldsstudio.hexfieldsdominion.account.error.UserAlreadyExistsException;
import de.hexfieldsstudio.hexfieldsdominion.account.token.RefreshTokensService;
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
    private final RefreshTokensService refreshTokensService;

    public AuthenticationResult guest() {
        String guestUsername;
        do {
            guestUsername = "Guest_%s".formatted(UUID.randomUUID().toString());
            // make sure the username is unique
        } while (userRepository.findByUsername((guestUsername)).isPresent());

        User user = User.builder()
                .username(guestUsername)
                .role(Role.GUEST)
                .build();
        userRepository.save(user);

        return this.createNewTokensAndGetResult(user);
    }

    public AuthenticationResult register(RegisterDTO request) throws InvalidCharactersException {
        if (!VALID_USERNAME_PW_PATTERN.matcher(request.username()).matches() || !VALID_USERNAME_PW_PATTERN.matcher(request.password()).matches()) {
            throw new InvalidCharactersException();
        }

        if (userRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        User user = User.builder()
                .username(request.username())
                .password(request.password(), passwordEncoder)
                .role(Role.PLAYER)
                .build();
        userRepository.save(user);

        return this.createNewTokensAndGetResult(user);
    }

    public AuthenticationResult login(LoginDTO request) throws InvalidCredentialsException {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return this.createNewTokensAndGetResult(user);
    }

    public Optional<AuthenticationResult> refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            return Optional.empty();
        }

        String username = jwtService.extractUsername(refreshToken);
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        User user = userOptional.get();

        if (!refreshTokensService.isValid(user, refreshToken)) {
            return Optional.empty();
        }

        return Optional.of(this.createNewTokensAndGetResult(user));
    }

    public Optional<AuthenticationResult> logout(String oldRefreshToken) {
        if (oldRefreshToken != null) {
            String username = jwtService.extractUsername(oldRefreshToken);
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                return Optional.empty();
            }
            User user = userOptional.get();

            refreshTokensService.invalidate(user, userRepository);
        }

        return Optional.of(AuthenticationResult.builder()
                .refreshTokenCookie(cookieService.createDeleteRefreshTokenCookie())
                .build());
    }

    private AuthenticationResult createNewTokensAndGetResult(User user) {
        String accessToken = jwtService.generateToken(user, ACCESS_TOKEN_MAX_AGE);
        AuthenticationResponse response = new AuthenticationResponse(accessToken);

        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(user);
        refreshTokensService.store(user, refreshTokenCookie, userRepository);

        return AuthenticationResult.builder()
                .authenticationResponse(response)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

}
