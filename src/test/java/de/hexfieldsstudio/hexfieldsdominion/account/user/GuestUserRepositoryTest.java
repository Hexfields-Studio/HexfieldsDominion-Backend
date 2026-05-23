package de.hexfieldsstudio.hexfieldsdominion.account.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
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

    @Test
    public void testFindByUsernameIgnoreCase() {
        guestUserRepository.save(user);

        Optional<User> optionalUserValidUppercase = guestUserRepository.findByUsernameIgnoreCase(user.getUsername().toUpperCase());
        Optional<User> optionalUserValidLowercase = guestUserRepository.findByUsernameIgnoreCase(user.getUsername().toLowerCase());
        Optional<User> optionalUserValidEqual = guestUserRepository.findByUsernameIgnoreCase(user.getUsername());
        Optional<User> optionalUserInvalid = guestUserRepository.findByUsernameIgnoreCase("otherUser");

        assertTrue(optionalUserValidUppercase.isPresent());
        assertTrue(optionalUserValidLowercase.isPresent());
        assertTrue(optionalUserValidEqual.isPresent());
        assertEquals(user, optionalUserValidUppercase.get());
        assertEquals(user, optionalUserValidLowercase.get());
        assertEquals(user, optionalUserValidEqual.get());

        assertFalse(optionalUserInvalid.isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteAll() throws NoSuchFieldException, IllegalAccessException {
        guestUserRepository.save(user);
        Field field = GuestUserRepository.class.getDeclaredField("guestUsers");
        field.setAccessible(true);

        guestUserRepository.deleteAll();

        assertTrue(((Map<String, User>) field.get(guestUserRepository)).isEmpty());
        field.setAccessible(false);
    }

}
