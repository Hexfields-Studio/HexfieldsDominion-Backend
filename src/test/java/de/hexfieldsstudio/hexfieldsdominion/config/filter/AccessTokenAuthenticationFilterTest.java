package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import jakarta.servlet.http.HttpServletRequest;
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
class AccessTokenAuthenticationFilterTest {

    @InjectMocks
    private AccessTokenAuthenticationFilter accessTokenAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @ParameterizedTest
    @ValueSource(strings = {
            "/auth/refresh",
            "/lobbies/xyz/events"
    })
    void testShouldNotFilter(String path) {
        when(request.getRequestURI()).thenReturn(path);

        assertTrue(accessTokenAuthenticationFilter.shouldNotFilter(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/auth",
            "lobbies/code",
            "/"
    })
    void testShouldFilter(String path) {
        when(request.getRequestURI()).thenReturn(path);

        assertFalse(accessTokenAuthenticationFilter.shouldNotFilter(request));
    }

}
