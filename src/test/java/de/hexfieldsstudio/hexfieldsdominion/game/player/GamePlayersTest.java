package de.hexfieldsstudio.hexfieldsdominion.game.player;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GamePlayersTest {

    private GamePlayers gamePlayers;

    private final User[] initialUsers = new User[]{
            User.builder().username("test").role(Role.GUEST).id(0).build(),
            User.builder().username("test2").role(Role.GUEST).id(1).build(),
            User.builder().username("test3").role(Role.GUEST).id(2).build()
    };

    @BeforeEach
    void setupEach() {
        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.getHeartbeatCheckIntervalSeconds()).thenReturn(5L);

        Lobby lobby = spy(new Lobby(appConfig));
        when(lobby.getPlayers()).thenReturn(List.of(
                new Player(initialUsers[0], initialUsers[0].getId()),
                new Player(initialUsers[1], initialUsers[1].getId()),
                new Player(initialUsers[2], initialUsers[2].getId())
        ));

        gamePlayers = new GamePlayers(lobby);
    }

    @Test
    void testRotateNextPlayer() {
        List<Integer> orderBefore = List.copyOf(gamePlayers.getPlayersTurnOrder());

        gamePlayers.rotateNextPlayer();

        assertEquals(orderBefore.getFirst(), gamePlayers.getPlayersTurnOrder().getLast());
        assertEquals(orderBefore.get(1), gamePlayers.getPlayersTurnOrder().getFirst());
        assertEquals(orderBefore.getLast(), gamePlayers.getPlayersTurnOrder().get(1));
    }

    @Test
    void testGetPlayerCurrentTurn() {
        assertEquals(gamePlayers.getPlayersTurnOrder().getFirst(), gamePlayers.getPlayerCurrentTurn());
    }

    @Test
    void testIsPlayersTurn() {
        User userTrue = initialUsers[gamePlayers.getPlayersTurnOrder().getFirst()];
        User userFalse = initialUsers[gamePlayers.getPlayersTurnOrder().get(1)];

        assertTrue(gamePlayers.isPlayersTurn(userTrue));
        assertFalse(gamePlayers.isPlayersTurn(userFalse));
    }

    @Test
    void testGetPlayerForUser() {
        Optional<PlayerRepresentation> playerOptionalNull = gamePlayers.getPlayerForUser(null);
        Optional<PlayerRepresentation> playerOptionalUnknown = gamePlayers.getPlayerForUser(User.builder().username("unknownUser").build());
        User existingUser = initialUsers[0];
        Optional<PlayerRepresentation> playerOptionalExisting = gamePlayers.getPlayerForUser(existingUser);

        assertTrue(playerOptionalNull.isEmpty());
        assertTrue(playerOptionalUnknown.isEmpty());

        assertTrue(playerOptionalExisting.isPresent());
        assertEquals(existingUser.getUsername(), playerOptionalExisting.get().getUsername());
    }

    @Test
    void testGetPlayerById() {
        Optional<PlayerRepresentation> playerOptionalUnknown = gamePlayers.getPlayerById(10);
        User existingUser = initialUsers[0];
        Optional<PlayerRepresentation> playerOptionalExisting = gamePlayers.getPlayerById(existingUser.getId());

        assertTrue(playerOptionalUnknown.isEmpty());

        assertTrue(playerOptionalExisting.isPresent());
        assertEquals(existingUser.getUsername(), playerOptionalExisting.get().getUsername());
    }

}
