package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.AllUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AllUserRepository userRepository;

    public static boolean doesFilter(String path) {
        return path.matches("/lobbies/[a-zA-Z0-9]+/events");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !doesFilter(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String queryString = request.getQueryString();
        if (queryString == null || !queryString.contains("accessToken=")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract accessToken from query string (e.g., ?accessToken=xyz)
        String accessToken = extractAccessToken(queryString);
        if (accessToken == null || !jwtService.isTokenValid(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(accessToken);
        userRepository.findByUsername(username).ifPresent(user -> {
            Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        });

        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(String queryString) {
        // Simple extraction: find accessToken=...& or end
        int start = queryString.indexOf("accessToken=");
        if (start == -1) return null;
        start += "accessToken=".length();
        int end = queryString.indexOf('&', start);
        if (end == -1) end = queryString.length();
        return queryString.substring(start, end);
    }
}
