package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens;
import de.hexfieldsstudio.hexfieldsdominion.account.token.CookieService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.RefreshTokensService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static de.hexfieldsstudio.hexfieldsdominion.config.filter.FilterITUtils.createDummyFilterChain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class RefreshTokenAuthenticationFilterIT {

    @Autowired
    private RefreshTokenAuthenticationFilter filter;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokensService refreshTokensService;

    @Autowired
    private AllUserRepository allUserRepository;

    @Autowired
    private GuestUserRepository guestUserRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CookieService cookieService;

    @BeforeEach
    void setupEach() {
        allUserRepository.deleteAll();
    }

    @Test
    void testFilterSuccess() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        Cookie cookie = cookieService.createRefreshTokenCookie(user);
        user.setRefreshToken(cookie.getValue(), passwordEncoder);

        allUserRepository.save(user);

        this.doFilterInternalWithCookie(cookie);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(user, authentication.getPrincipal());
        assertEquals(user.getAuthorities(), authentication.getAuthorities());
    }

    @ParameterizedTest
    @ArgumentsSource(MissingCookieProvider.class)
    void testFilterFailMissingCookie(Cookie[] cookies) throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        this.doFilterInternalWithCookies(cookies);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterFailAlreadyAuthenticated() throws ServletException, IOException {
        Authentication authentication = new UsernamePasswordAuthenticationToken("principal", "credentials");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        Cookie cookie = cookieService.createRefreshTokenCookie(user);
        user.setRefreshToken(cookie.getValue(), passwordEncoder);

        allUserRepository.save(user);

        this.doFilterInternalWithCookie(cookie);

        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterFailExpiredToken() throws ServletException, IOException, InterruptedException {
        SecurityContextHolder.getContext().setAuthentication(null);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        Cookie cookie = new Cookie(AuthTokens.REFRESH_TOKEN_NAME, jwtService.generateToken(user, 1));
        user.setRefreshToken(cookie.getValue(), passwordEncoder);

        allUserRepository.save(user);

        // wait for token to expire
        Thread.sleep(1001);

        this.doFilterInternalWithCookie(cookie);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterFailUnknownUser() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        Cookie cookie = cookieService.createRefreshTokenCookie(user);

        this.doFilterInternalWithCookie(cookie);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterFailInvalidTokenForUser() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .refreshToken("otherToken", passwordEncoder)
                .build();

        Cookie cookie = cookieService.createRefreshTokenCookie(user);

        allUserRepository.save(user);

        this.doFilterInternalWithCookie(cookie);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterFailNoTokenStoredForUser() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);

        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        Cookie cookie = cookieService.createRefreshTokenCookie(user);

        allUserRepository.save(user);

        this.doFilterInternalWithCookie(cookie);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private void doFilterInternalWithCookie(Cookie cookie) throws ServletException, IOException {
        this.doFilterInternalWithCookies(new Cookie[]{cookie});
    }

    private void doFilterInternalWithCookies(Cookie[] cookies) throws ServletException, IOException {
        HttpServletRequest request = this.createRequestWithCookies(cookies);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = createDummyFilterChain();

        filter.doFilterInternal(request, response, filterChain);
    }

    private HttpServletRequest createRequestWithCookies(Cookie[] cookies) {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getCookies()).thenReturn(cookies);
        return request;
    }

    static class MissingCookieProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(null, List.of()),
                    Arguments.of(new Cookie[]{new Cookie("irrelevantName", "val")}, List.of())
            );
        }
    }

}
