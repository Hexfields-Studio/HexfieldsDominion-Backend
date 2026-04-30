package de.hexfieldsstudio.hexfieldsdominion.account.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GuestUserRepositoryTest {

    private GuestUserRepository guestUserRepository;
    private User user;

    @BeforeEach
    public void setupEach() {
        guestUserRepository = new GuestUserRepository();
        user = User.builder()
                .username("testuser")
                .build();
    }

    @Test
    public void testSave() {
        guestUserRepository.save(user);

        Optional<User> userOptional = guestUserRepository.findByUsername(user.getUsername());

        assertTrue(userOptional.isPresent());
        assertEquals(user, userOptional.get());
    }

    @Test
    public void testFindByUsername() {
        guestUserRepository.save(user);

        Optional<User> optionalUserValid = guestUserRepository.findByUsername(user.getUsername());
        Optional<User> optionalUserInvalid = guestUserRepository.findByUsername("otherUser");

        assertTrue(optionalUserValid.isPresent());
        assertEquals(user, optionalUserValid.get());

        assertFalse(optionalUserInvalid.isPresent());
    }

}
