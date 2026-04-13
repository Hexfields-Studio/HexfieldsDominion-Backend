package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.AllUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

@Component
@RequiredArgsConstructor
public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AllUserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return RefreshTokenAuthenticationFilter.doesFilter(path);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ") || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring("Bearer ".length());

        if (jwtService.isTokenValid(accessToken)) {
            String username = jwtService.extractUsername(accessToken);

            userRepository.findByEmail(username).ifPresent(user -> {
                Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        }
        filterChain.doFilter(request, response);
    }

}
