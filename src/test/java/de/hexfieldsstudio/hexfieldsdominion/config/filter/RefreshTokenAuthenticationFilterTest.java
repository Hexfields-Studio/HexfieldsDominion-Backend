package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenAuthenticationFilterTest {

    @InjectMocks
    private RefreshTokenAuthenticationFilter refreshTokenAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @ParameterizedTest
    @ValueSource(strings = {
            "/auth",
            "/auth/login",
            "lobbies",
            "/",
            "/lobbies/xyz/events"
    })
    public void testShouldNotFilter(String path) {
        when(request.getRequestURI()).thenReturn(path);

        assertTrue(refreshTokenAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    public void testShouldFilter() {
        String path = "/auth/refresh";

        when(request.getRequestURI()).thenReturn(path);

        assertFalse(refreshTokenAuthenticationFilter.shouldNotFilter(request));
    }

}
