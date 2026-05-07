package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import java.io.IOException;
import java.util.Optional;

import de.hexfieldsstudio.hexfieldsdominion.account.token.SseTokenService;
import lombok.NonNull;
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
    private final SseTokenService sseTokenService;
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
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String queryString = request.getQueryString();
        if (queryString == null || !queryString.contains("sseToken=")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract sseToken from query string (e.g., ?sseToken=xyz)
        Optional<String> sseTokenOptional = extractSseToken(queryString);
        if (sseTokenOptional.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        String sseToken = sseTokenOptional.get();

        String username = jwtService.extractUsername(sseToken);
        userRepository.findByUsername(username).ifPresent(user -> {
            Optional<String> storedValidToken = sseTokenService.getValidTokenAndInvalidate(user);
            if (storedValidToken.isEmpty() || !sseToken.equals(storedValidToken.get())) {
                return;
            }

            Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        });

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractSseToken(String queryString) {
        // Simple extraction: find sseToken=...& or end
        int start = queryString.indexOf("sseToken=");
        if (start == -1) return Optional.empty();
        start += "sseToken=".length();
        int end = queryString.indexOf('&', start);
        if (end == -1) end = queryString.length();
        return Optional.of(queryString.substring(start, end));
    }
}
