package de.hexfieldsstudio.hexfieldsdominion.game.player;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    void testGetUsername() {
        User user = User.builder()
                .username("test")
                .role(Role.GUEST)
                .build();

        Player player = new Player(user, 0);

        assertEquals(user.getUsername(), player.getUsername());
    }

    @Test
    void testIsAccountTrue() {
        User user = User.builder()
                .username("test")
                .role(Role.PLAYER)
                .build();

        Player player = new Player(user, 0);

        assertTrue(player.isAccount());
    }

    @Test
    void testIsAccountFalse() {
        User user = User.builder()
                .username("test")
                .role(Role.GUEST)
                .build();

        Player player = new Player(user, 0);

        assertFalse(player.isAccount());
    }

}
