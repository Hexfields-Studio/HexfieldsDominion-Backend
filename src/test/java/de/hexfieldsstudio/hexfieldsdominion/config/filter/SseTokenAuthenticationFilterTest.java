package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SseTokenAuthenticationFilterTest {

    @InjectMocks
    private SseTokenAuthenticationFilter sseTokenAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @ParameterizedTest
    @ValueSource(strings = {
            "/auth",
            "/auth/login",
            "lobbies",
            "/",
            "/auth/refresh"
    })
    public void testShouldNotFilter(String path) {
        when(request.getRequestURI()).thenReturn(path);

        assertTrue(sseTokenAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    public void testShouldFilter() {
        String path = "/lobbies/xyz/events";

        when(request.getRequestURI()).thenReturn(path);

        assertFalse(sseTokenAuthenticationFilter.shouldNotFilter(request));
    }

}
