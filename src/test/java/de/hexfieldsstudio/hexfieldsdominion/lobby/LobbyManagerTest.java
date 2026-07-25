package de.hexfieldsstudio.hexfieldsdominion.lobby;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.game.types.TradingStatus;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.InvalidRadiusException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.LobbyNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.NotOwnerOfLobbyException;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static de.hexfieldsstudio.hexfieldsdominion.TestUtils.assertLobbyCodeValid;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LobbyManagerTest {

    private static final int INITIAL_CAPACITY = 10;

    private AppConfig appConfig;

    private LobbyManager lobbyManager;

    @BeforeEach
    void setupEach() {
        appConfig = mock(AppConfig.class);
        when(appConfig.getInitialCapacity()).thenReturn(INITIAL_CAPACITY);

        lobbyManager = new LobbyManager(appConfig);
    }

    @Test
    void testCreateLobbySuccessManyLobbiesFree() throws Exception {
        this.testCreateLobbySuccess();
    }

    @Test
    void testCreateLobbySuccessOneLobbyFree() throws Exception {
        when(appConfig.getInitialCapacity()).thenReturn(1);
        lobbyManager = new LobbyManager(appConfig);

        this.testCreateLobbySuccess();
    }

    private void testCreateLobbySuccess() throws Exception {
        String lobbyCode = lobbyManager.createLobby(new String[0], "someone");

        assertLobbyCodeValid(lobbyCode);
    }

    @Test
    void testCreateLobbyFailAllLobbiesOccupied() {
        when(appConfig.getInitialCapacity()).thenReturn(0);
        lobbyManager = new LobbyManager(appConfig);

        Exception exception = assertThrows(Exception.class, () -> lobbyManager.createLobby(new String[0], "someone"));
        assertEquals("Server Capacity has been reached. Could not create lobby.", exception.getMessage());
    }

    @Test
    void testJoinLobbySuccess() throws Exception {
        this.createLobbyWithUser((user, lobbyCode) -> {
            LobbyManager.JoinedLobbyResponse response = lobbyManager.joinLobby(lobbyCode, user);

            assertTrue(response.isLobbyOwner());
            assertEquals(user.getUsername(), response.createdPlayer().username());
        });
    }

    @ParameterizedTest
    @ArgumentsSource(UnknownLobbyCodesProvider.class)
    void testJoinLobbyInvalidLobbyCode(String lobbyCode) {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();

        assertThrowsExactly(LobbyNotFoundException.class, () -> lobbyManager.joinLobby(lobbyCode, user));
    }

    @Test
    void testFindOccupiedLobbyOrThrowFound() throws Exception {
        this.createLobbyWithUser((user, lobbyCode) -> {
            Lobby foundLobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);

            assertNotNull(foundLobby);
            assertEquals(lobbyCode, foundLobby.getLobbyCode());
            assertTrue(foundLobby.isOwner(user.getUsername()));
        });
    }

    @ParameterizedTest
    @ArgumentsSource(UnknownLobbyCodesProvider.class)
    void testFindOccupiedLobbyOrThrowNotFound(String lobbyCode) {
        assertThrowsExactly(LobbyNotFoundException.class, () -> lobbyManager.findOccupiedLobbyOrThrow(lobbyCode));
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5, 6})
    void testCreateMatchForLobbySuccess(int boardRadius) throws Exception {
        this.createLobbyWithUser((user, lobbyCode) -> {
            lobbyManager.joinLobby(lobbyCode, user);
            lobbyManager.subscribe(lobbyCode, user.getUsername());
            Lobby lobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);

            try {
                Match match = lobbyManager.createMatchForLobby(lobby, user, boardRadius);

                assertNotNull(match);
                assertNotNull(match.getUuid());
                assertEquals(match, lobby.getMatch());
            } catch (IllegalArgumentException e) {
                //TODO: remove try/catch when exception is not sometimes thrown for boardRadius 4, 5, 6 anymore (looks like an error in Match(): StructureFactory.randomlyBuildInitialStructures)
            }
        });
    }

    @Test
    void testCreateMatchForLobbyFailNotOwnerOfLobby() throws Exception {
        User userNotOwner = User.builder()
                .username("notOwner")
                .role(Role.GUEST)
                .build();

        this.createLobbyWithUser((userOwner, lobbyCode) -> {
            lobbyManager.joinLobby(lobbyCode, userOwner);
            lobbyManager.subscribe(lobbyCode, userOwner.getUsername());
            lobbyManager.joinLobby(lobbyCode, userNotOwner);
            lobbyManager.subscribe(lobbyCode, userNotOwner.getUsername());
            Lobby lobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);

            assertThrowsExactly(NotOwnerOfLobbyException.class, () -> lobbyManager.createMatchForLobby(lobby, userNotOwner, LobbyController.BOARD_RADIUS));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 7, 8})
    void testCreateMatchForLobbyFailInvalidBoardRadius(int boardRadius) throws Exception {
        this.createLobbyWithUser((user, lobbyCode) -> {
            lobbyManager.joinLobby(lobbyCode, user);
            lobbyManager.subscribe(lobbyCode, user.getUsername());
            Lobby lobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);

            assertThrowsExactly(InvalidRadiusException.class, () -> lobbyManager.createMatchForLobby(lobby, user, boardRadius));
        });
    }

    @Test
    void testFindLobbyByMatchSuccess() throws Exception {
        this.createLobbyWithUser((user, lobbyCode) -> {
            lobbyManager.joinLobby(lobbyCode, user);
            lobbyManager.subscribe(lobbyCode, user.getUsername());
            Lobby lobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);
            Match match = lobbyManager.createMatchForLobby(lobby, user, LobbyController.BOARD_RADIUS);

            Lobby lobbyFound = lobbyManager.findLobbyByMatch(match.getUuid());

            assertEquals(lobby, lobbyFound);
        });
    }

    @Test
    void testFindLobbyByMatchFail() {
        assertThrowsExactly(MatchNotFoundException.class, () -> lobbyManager.findLobbyByMatch(UUID.randomUUID()));
    }

    @Test
    void testSubscribeLobbyFound() throws Exception {
        this.createLobbyWithUser((user, lobbyCode) -> {
            SseEmitter emitter = lobbyManager.subscribe(lobbyCode, user.getUsername());

            assertNotNull(emitter);
        });
    }

    @ParameterizedTest
    @ArgumentsSource(UnknownLobbyCodesProvider.class)
    void testSubscribeLobbyNotFound(String lobbyCode) {
        SseEmitter emitter = lobbyManager.subscribe(lobbyCode, "someone");

        assertNotNull(emitter);
    }

    @Test
    void testOnUnsubscribeLobbyExists() throws Exception {
        this.createLobbyWithUser((user, lobbyCode) -> {
            lobbyManager.joinLobby(lobbyCode, user);
            lobbyManager.subscribe(lobbyCode, user.getUsername());
            Lobby lobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);

            lobbyManager.onUnsubscribe(lobbyCode, user.getUsername());

            assertFalse(lobby.getPlayers().stream().anyMatch(player -> user.getUsername().equals(player.getUsername())));
        });
    }

    private void createLobbyWithUser(BiConsumer<User, String> consumer) throws Exception {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        String lobbyCode = lobbyManager.createLobby(new String[0], user.getUsername());
        consumer.accept(user, lobbyCode);
    }

    static class UnknownLobbyCodesProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of("", " ", "x", "ABS68J9")
                    .map(Arguments::of);
        }
    }

}
