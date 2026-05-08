package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.SseTokenService;
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

import static de.hexfieldsstudio.hexfieldsdominion.config.filter.FilterITUtils.createDummyFilterChain;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class SseTokenAuthenticationFilterIT {

    @Autowired
    private SseTokenAuthenticationFilter filter;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SseTokenService sseTokenService;

    @Autowired
    private AllUserRepository allUserRepository;

    @Autowired
    private GuestUserRepository guestUserRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    private User user;

    @BeforeEach
    public void setupEach() {
        user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
    }

    @Test
    public void testFilterSuccess() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        allUserRepository.save(user);

        String createdToken = sseTokenService.createToken(user);

        HttpServletRequest request = this.createRequestWithValidQuery(createdToken);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(user, authentication.getPrincipal());
        assertEquals(user.getAuthorities(), authentication.getAuthorities());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "xyz=x"})
    public void testFilterFailMissingQueryParam(String query) throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        HttpServletRequest request = this.createRequestWithQuery(query);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailAlreadyAuthenticated() throws ServletException, IOException {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "cred");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String createdToken = sseTokenService.createToken(user);

        HttpServletRequest request = this.createRequestWithValidQuery(createdToken);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailProvidedTokenInvalid() throws ServletException, IOException, InterruptedException {
        SecurityContextHolder.getContext().setAuthentication(null);

        allUserRepository.save(user);

        String token = jwtService.generateToken(user, 1);

        HttpServletRequest request = this.createRequestWithValidQuery(token);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        // wait for token to expire
        Thread.sleep(1001);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailUnknownUser() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        String createdToken = sseTokenService.createToken(user);

        HttpServletRequest request = this.createRequestWithValidQuery(createdToken);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailNoValidTokenStoredForUser() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        allUserRepository.save(user);

        String tokenNotStored = jwtService.generateToken(user, 100);

        HttpServletRequest request = this.createRequestWithValidQuery(tokenNotStored);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testFilterFailProvidedTokenNotEqualsStored() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        allUserRepository.save(user);

        sseTokenService.createToken(user);
        String otherToken = jwtService.generateToken(user, 100);

        HttpServletRequest request = this.createRequestWithValidQuery(otherToken);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private HttpServletRequest createRequestWithValidQuery(String sseToken) {
        return this.createRequestWithQuery("sseToken=%s".formatted(sseToken));
    }

    private HttpServletRequest createRequestWithQuery(String query) {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getQueryString()).thenReturn(query);
        return request;
    }

}
