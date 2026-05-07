package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SseTokenService {

    private final JwtService jwtService;
    private final Map<String, String> usernamesTokens = new HashMap<>();

    public String createToken(User user) {
        String token = jwtService.generateToken(user, AuthTokens.SSE_TOKEN_MAX_AGE);

        this.usernamesTokens.put(user.getUsername(), token);
        return token;
    }

    public Optional<String> getValidTokenAndInvalidate(User user) {
        if (!this.usernamesTokens.containsKey(user.getUsername())) {
            return Optional.empty();
        }

        String storedToken = this.usernamesTokens.get(user.getUsername());

        if (!jwtService.isTokenValid(storedToken)) {
            this.usernamesTokens.remove(user.getUsername());
            return Optional.empty();
        }

        this.usernamesTokens.remove(user.getUsername());
        return Optional.of(storedToken);
    }

}
