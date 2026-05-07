package de.hexfieldsstudio.hexfieldsdominion.account.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    private static final String PASSWORD = "password";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String ENCODED_REFRESH_TOKEN = "encodedRefreshToken";

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordsHashedBuilder() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(passwordEncoder.encode(REFRESH_TOKEN)).thenReturn(ENCODED_REFRESH_TOKEN);

        User user = User.builder()
                .password(PASSWORD, passwordEncoder)
                .refreshToken(REFRESH_TOKEN, passwordEncoder)
                .build();

        assertEquals(ENCODED_PASSWORD, user.getPassword());
        assertEquals(ENCODED_REFRESH_TOKEN, user.getRefreshToken());
    }

    @Test
    public void testPasswordsHashedSetter() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(passwordEncoder.encode(REFRESH_TOKEN)).thenReturn(ENCODED_REFRESH_TOKEN);

        User user = new User();
        user.setPassword(PASSWORD, passwordEncoder);
        user.setRefreshToken(REFRESH_TOKEN, passwordEncoder);

        assertEquals(ENCODED_PASSWORD, user.getPassword());
        assertEquals(ENCODED_REFRESH_TOKEN, user.getRefreshToken());
    }

}
