package de.hexfieldsstudio.hexfieldsdominion.account.token;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Value("${app.jwt.secretKey}")
    private String SECRET_KEY;

    @Autowired
    private JwtService jwtService;

    @Test
    void testExtractClaims() {
        final long currentMillis = System.currentTimeMillis();
        final Date issuedAt = new Date(currentMillis);
        final Date expiration = new Date(currentMillis + 5000);
        final String subject = "testuser";

        String jwt = Jwts.builder()
                .issuedAt(issuedAt)
                .expiration(expiration)
                .subject(subject)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .compact();

        assertEquals(subject, jwtService.extractUsername(jwt));
        // div by 1000 because of jwt value rounded (floored i.g.)
        assertEquals(issuedAt.getTime() / 1000, jwtService.extractClaim(jwt, Claims::getIssuedAt).getTime() / 1000);
        assertEquals(expiration.getTime() / 1000, jwtService.extractClaim(jwt, Claims::getExpiration).getTime() / 1000);
    }

    @Test
    void testGenerateToken() {
        User user = User.builder()
                .username("testuser")
                .build();

        int maxAgeSeconds = 10;

        String token = jwtService.generateToken(user, maxAgeSeconds);

        long timeIssuedAt = jwtService.extractClaim(token, Claims::getIssuedAt).getTime();
        long timeExpiration = jwtService.extractClaim(token, Claims::getExpiration).getTime();
        assertEquals(maxAgeSeconds * 1000, timeExpiration - timeIssuedAt);
        assertEquals(user.getUsername(), jwtService.extractUsername(token));
    }

    @Test
    void testGenerateTokenExtraClaims() {
        User user = User.builder()
                .username("testuser")
                .build();

        Map<String, Object> extraClaims = Map.of(
                "claim1", "val1",
                "claim2", "val2"
        );

        String token = jwtService.generateToken(extraClaims, user, 10);

        assertEquals(extraClaims.get("claim1"), jwtService.extractClaim(token, c -> c.get("claim1")));
        assertEquals(extraClaims.get("claim2"), jwtService.extractClaim(token, c -> c.get("claim2")));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "xyz"})
    void testIsTokenValid(String tokenInvalidNoJwt) throws InterruptedException {
        User user = User.builder()
                .username("testuser")
                .build();

        String tokenValid = jwtService.generateToken(user, 10);
        String tokenInvalidExpired = jwtService.generateToken(user, 1);

        // wait until tokenInvalidExpired is invalid
        Thread.sleep(1001);

        assertTrue(jwtService.isTokenValid(tokenValid));
        assertFalse(jwtService.isTokenValid(tokenInvalidExpired));
        assertFalse(jwtService.isTokenValid(tokenInvalidNoJwt));
    }

}
