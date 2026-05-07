package de.hexfieldsstudio.hexfieldsdominion.account;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new RuntimeException("Unauthorized");
        }
        return user;
    }

}
