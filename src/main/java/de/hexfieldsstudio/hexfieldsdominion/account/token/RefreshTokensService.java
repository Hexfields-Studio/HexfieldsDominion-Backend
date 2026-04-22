package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.account.user.UserRepository;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RefreshTokensService {

    private final PasswordEncoder passwordEncoder;

    public void store(User user, Cookie refreshTokenCookie, UserRepository userRepository) {
        user.setRefreshToken(refreshTokenCookie.getValue(), passwordEncoder);
        userRepository.save(user);
    }

    public void invalidate(User user, UserRepository userRepository) {
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    public boolean isValid(User user, @NonNull String refreshToken) {
        return passwordEncoder.matches(refreshToken, user.getRefreshToken());
    }

}
