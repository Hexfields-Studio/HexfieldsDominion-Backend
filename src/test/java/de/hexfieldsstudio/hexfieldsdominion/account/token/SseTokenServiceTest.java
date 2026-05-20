package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SseTokenServiceTest {

    private static Field fieldUsernamesTokens;

    @InjectMocks
    private SseTokenService sseTokenService;

    @Mock
    private JwtService jwtService;

    private Map<String, String> storedUsernamesTokens;

    @BeforeAll
    public static void setup() throws NoSuchFieldException {
        fieldUsernamesTokens = SseTokenService.class.getDeclaredField("usernamesTokens");
        fieldUsernamesTokens.setAccessible(true);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setupEach() throws IllegalAccessException {
        storedUsernamesTokens = (Map<String, String>) fieldUsernamesTokens.get(sseTokenService);
        storedUsernamesTokens.clear();
    }

    @AfterAll
    public static void afterAll() {
        fieldUsernamesTokens.setAccessible(false);
    }

    @Test
    public void testCreateToken() {
        User user = User.builder()
                .username("testuser")
                .build();

        String mockSseToken = "sseToken";

        when(jwtService.generateToken(user, AuthTokens.SSE_TOKEN_MAX_AGE)).thenReturn(mockSseToken);

        String createdToken = sseTokenService.createToken(user);

        assertEquals(mockSseToken, createdToken);
        assertTrue(storedUsernamesTokens.containsKey(user.getUsername()));
        assertEquals(mockSseToken, storedUsernamesTokens.get(user.getUsername()));
    }

    @Test
    public void testGetValidTokenAndInvalidateSuccess() {
        User user = User.builder()
                .username("testuser")
                .build();

        String mockSseToken = "sseToken";

        when(jwtService.generateToken(user, AuthTokens.SSE_TOKEN_MAX_AGE)).thenReturn(mockSseToken);

        String createdToken = sseTokenService.createToken(user);

        when(jwtService.isTokenValid(createdToken)).thenReturn(true);

        Optional<String> storedTokenOptional = sseTokenService.getValidTokenAndInvalidate(user);

        assertTrue(storedTokenOptional.isPresent());
        assertEquals(createdToken, storedTokenOptional.get());
        assertFalse(storedUsernamesTokens.containsKey(user.getUsername()));
    }

    @Test
    public void testGetValidTokenAndInvalidateNoTokenStoredForUser() {
        User user = User.builder()
                .username("testuser")
                .build();

        Optional<String> storedTokenOptional = sseTokenService.getValidTokenAndInvalidate(user);

        assertFalse(storedTokenOptional.isPresent());
    }

    @Test
    public void testGetValidTokenAndInvalidateStoredTokenExpired() {
        User user = User.builder()
                .username("testuser")
                .build();

        String mockSseToken = "sseToken";

        when(jwtService.generateToken(user, AuthTokens.SSE_TOKEN_MAX_AGE)).thenReturn(mockSseToken);

        String createdToken = sseTokenService.createToken(user);

        when(jwtService.isTokenValid(createdToken)).thenReturn(false);

        Optional<String> storedTokenOptional = sseTokenService.getValidTokenAndInvalidate(user);

        assertFalse(storedTokenOptional.isPresent());
        assertFalse(storedUsernamesTokens.containsKey(user.getUsername()));
    }

}
