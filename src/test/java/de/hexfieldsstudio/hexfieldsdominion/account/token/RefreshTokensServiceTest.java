package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.GuestUserRepository;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.account.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokensServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RefreshTokensService refreshTokensService;

    private User user;

    @BeforeEach
    public void setupEach() {
        user = User.builder()
                .username("testuser")
                .build();
    }

    @Test
    public void testStore() {
        Cookie cookie = new Cookie("refreshToken", "testvalue");
        UserRepository userRepository = new GuestUserRepository();
        userRepository.save(user);

        when(passwordEncoder.encode(cookie.getValue())).thenReturn(cookie.getValue());

        refreshTokensService.store(user, cookie, userRepository);

        assertEquals(cookie.getValue(), user.getRefreshToken());
        assertTrue(userRepository.findByUsername(user.getUsername()).isPresent());
        assertEquals(cookie.getValue(), userRepository.findByUsername(user.getUsername()).get().getRefreshToken());
    }

    @Test
    public void testInvalidate() {
        UserRepository userRepository = new GuestUserRepository();
        userRepository.save(user);

        refreshTokensService.invalidate(user, userRepository);

        assertNull(user.getRefreshToken());
        assertTrue(userRepository.findByUsername(user.getUsername()).isPresent());
        assertNull(userRepository.findByUsername(user.getUsername()).get().getRefreshToken());
    }

    @Test
    public void testIsValidTrue() {
        String refreshToken = "testValue";

        when(passwordEncoder.encode(refreshToken)).thenReturn(refreshToken);
        when(passwordEncoder.matches(refreshToken, refreshToken)).thenReturn(true);

        user.setRefreshToken(refreshToken, passwordEncoder);

        assertTrue(refreshTokensService.isValid(user, refreshToken));
    }

    @Test
    public void testIsValidFalse() {
        String refreshToken = "testValue";
        String otherToken = "otherValue";

        when(passwordEncoder.encode(refreshToken)).thenReturn(refreshToken);
        when(passwordEncoder.matches(otherToken, refreshToken)).thenReturn(false);

        user.setRefreshToken(refreshToken, passwordEncoder);

        assertFalse(refreshTokensService.isValid(user, otherToken));
    }

}
