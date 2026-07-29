package de.hexfieldsstudio.hexfieldsdominion.game.player;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerRepresentationTest {

    @Test
    void testNewPlayerRepresentation() {
        User user = User.builder()
                .username("test")
                .role(Role.GUEST)
                .build();

        Player player = new Player(user, 0);

        PlayerRepresentation playerRepresentation = new PlayerRepresentation(player);

        assertEquals(player.getUsername(), playerRepresentation.getUsername());
        assertEquals(player.getId(), playerRepresentation.getPublicId());
        assertEquals(0, playerRepresentation.getPoints());
        assertTrue(playerRepresentation.getResources().isEmpty());

        int expectedHue = PlayerHueFactory.generateHueFromHash(player.getUsername());
        assertEquals(expectedHue, playerRepresentation.getPlayerHue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4, 11})
    void testAddPoints(int pointsToAdd) {
        User user = User.builder()
                .username("test")
                .role(Role.GUEST)
                .build();

        Player player = new Player(user, 0);

        PlayerRepresentation playerRepresentation = new PlayerRepresentation(player);
        playerRepresentation.setPoints(0);

        playerRepresentation.addPoints(pointsToAdd);

        assertEquals(pointsToAdd, playerRepresentation.getPoints());
    }

}
