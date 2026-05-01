package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AccessTokenAuthenticationFilterIT {

    @Autowired
    private AccessTokenAuthenticationFilter filter;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AllUserRepository allUserRepository;

    @Autowired
    private GuestUserRepository guestUserRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @BeforeEach
    public void setupEach() {
        allUserRepository.deleteAll();
    }

    @Test
    public void testFilterSuccess() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        allUserRepository.save(user);

        HttpServletRequest request = this.createRequestWithValidAuthHeader(user);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = this.createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(user, authentication.getPrincipal());
        assertEquals(user.getAuthorities(), authentication.getAuthorities());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "xyz"})
    public void testFilterFailAuthHeader(String authHeaderValue) throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        HttpServletRequest request = this.createRequestWithInvalidAuthHeader(authHeaderValue);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = this.createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailAlreadyAuthenticated() throws ServletException, IOException {
        Authentication authentication = new UsernamePasswordAuthenticationToken("principal", "crdentials");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        HttpServletRequest request = this.createRequestWithValidAuthHeader(user);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = this.createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailInvalidToken() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        HttpServletRequest request = this.createRequestWithInvalidAuthHeader("Bearer invalidToken");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = this.createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailUnknownUser() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        HttpServletRequest request = this.createRequestWithValidAuthHeader(user);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = this.createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private HttpServletRequest createRequestWithValidAuthHeader(User user) {
        HttpServletRequest request = mock(HttpServletRequest.class);

        String accessToken = jwtService.generateToken(user, 100);

        when(request.getHeader("Authorization")).thenReturn("Bearer %s".formatted(accessToken));
        return request;
    }

    private HttpServletRequest createRequestWithInvalidAuthHeader(String value) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(value);
        return request;
    }

    private FilterChain createDummyFilterChain() {
        return (request, response) -> {};
    }

}
