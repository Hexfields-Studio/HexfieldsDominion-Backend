package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AuthUtilsIT {

    @Test
    void testGetAuthenticatedUserSuccess() {
        User user = User.builder()
                .username("testuser")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User authenticatedUser = AuthUtils.getAuthenticatedUser();

        assertEquals(user, authenticatedUser);
    }

    @Test
    void testGetAuthenticatedUserFailNoAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(RuntimeException.class, AuthUtils::getAuthenticatedUser);
    }

    @Test
    void testGetAuthenticatedUserFailNotUser() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("invalid", null));

        assertThrows(RuntimeException.class, AuthUtils::getAuthenticatedUser);
    }

}
