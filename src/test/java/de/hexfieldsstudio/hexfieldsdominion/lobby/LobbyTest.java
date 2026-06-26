package de.hexfieldsstudio.hexfieldsdominion.lobby;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class LobbyTest {

    private Lobby lobby;

    private LobbyManager lobbyManager;

    @BeforeEach
    void setupEach() {
        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.getHeartbeatCheckIntervalSeconds()).thenReturn(5L);

        lobby = new Lobby(appConfig);

        lobbyManager = mock(LobbyManager.class);
    }

    @Test
    void testAddPlayerAlreadyExists() {
        Player player = this.createPlayer();

        lobby.getPlayers().add(player);

        Player storedPlayer = lobby.addPlayer(player.getUser(), lobbyManager);

        assertEquals(player, storedPlayer);
    }

    @Test
    void testAddPlayerNotExistsGuest() {
        this.testAddPlayerNotExists(Role.GUEST);

        assertFalse(lobby.isHasAccountPlayer());
    }

    @Test
    void testAddPlayerNotExistsAccount() {
        this.testAddPlayerNotExists(Role.PLAYER);

        assertTrue(lobby.isHasAccountPlayer());
    }

    private void testAddPlayerNotExists(Role role) {
        User user = User.builder()
                .username("test")
                .role(role)
                .build();

        Player storedPlayer = lobby.addPlayer(user, lobbyManager);

        assertEquals(user.getUsername(), storedPlayer.getUsername());
        Optional<Player> playerInListOptional = lobby.getPlayers().stream().filter(player -> player.getUsername().equals(user.getUsername())).findFirst();
        assertTrue(playerInListOptional.isPresent());
        assertEquals(storedPlayer.getId(), playerInListOptional.get().getId());
    }

    @Test
    void testRemovePlayerUsernameExists() {
        Player player = this.createPlayer();

        lobby.getPlayers().add(player);

        lobby.removePlayer(player.getUsername());

        assertTrue(lobby.getPlayers().isEmpty());
    }

    @Test
    void testRemovePlayerUsernameNotExists() {
        lobby.removePlayer("test");

        assertTrue(lobby.getPlayers().isEmpty());
    }

    @Test
    void testRemovePlayerIdExists() {
        Player player = this.createPlayer();

        lobby.getPlayers().add(player);

        lobby.removePlayer(player.getId());

        assertTrue(lobby.getPlayers().isEmpty());
    }

    @Test
    void testRemovePlayerIdNotExists() {
        Player player = this.createPlayer();

        lobby.getPlayers().add(player);

        lobby.removePlayer(1);

        assertEquals(1, lobby.getPlayers().size());
        assertEquals(player, lobby.getPlayers().getFirst());
    }

    @Test
    void testIsOwnerTrue() {
        Player player = this.createPlayer();

        lobby.getPlayers().add(player);
        lobby.setOwner(player.getUsername());

        assertTrue(lobby.isOwner(player.getUsername()));
    }

    @Test
    void testIsOwnerFalse() {
        Player player = this.createPlayer();

        lobby.getPlayers().add(player);
        lobby.setOwner("other");

        assertFalse(lobby.isOwner(player.getUsername()));
    }

    @Test
    void testOnNoHeartbeatPlayerExists() {
        Player player = this.createPlayer();

        lobby.getPlayers().add(player);

        lobby.onNoHeartbeat(lobby, player.getId());

        assertTrue(lobby.getPlayers().isEmpty());
    }

    @Test
    void testOnNoHeartbeatPlayerNotExists() {
        lobby.onNoHeartbeat(lobby, 0);

        assertTrue(lobby.getPlayers().isEmpty());
    }

    private Player createPlayer() {
        User user = User.builder()
                .username("test")
                .role(Role.GUEST)
                .build();

        return new Player(user, 0);
    }

}
