package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ValidRefreshTokensService {

    private final Map<String, String> usersValidTokens = new HashMap<>();

    public void store(User user, String refreshToken) {
        usersValidTokens.put(user.getEmail(), refreshToken);
    }

    public void invalidate(User user) {
        usersValidTokens.remove(user.getEmail());
    }

    public boolean isValid(String refreshToken) {
        return usersValidTokens.containsValue(refreshToken);
    }

}
