package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import de.hexfieldsstudio.hexfieldsdominion.account.token.AuthTokens;
import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.token.ValidRefreshTokensService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.AllUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ValidRefreshTokensService validRefreshTokensService;
    private final AllUserRepository userRepository;

    public static boolean doesFilter(String path) {
        return path.equals("/auth/refresh");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !doesFilter(path);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getCookies() == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Cookie> refreshTokenCookie = Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if (!cookie.getName().equals(AuthTokens.REFRESH_TOKEN_NAME)) {
                continue;
            }
            refreshTokenCookie = Optional.of(cookie);
        }

        if (refreshTokenCookie.isEmpty() || !validRefreshTokensService.isValid(refreshTokenCookie.get().getValue())) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = refreshTokenCookie.get().getValue();

        if (jwtService.isTokenValid(refreshToken)) {
            String username = jwtService.extractUsername(refreshToken);

            userRepository.findByUsername(username).ifPresent(user -> {
                Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        }

        filterChain.doFilter(request, response);
    }

}
