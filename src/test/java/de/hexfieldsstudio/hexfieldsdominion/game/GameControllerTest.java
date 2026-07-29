package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.account.AuthUtils;
import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Field;
import de.hexfieldsstudio.hexfieldsdominion.game.board.GameBoard;
import de.hexfieldsstudio.hexfieldsdominion.game.board.StructureFactory;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PlayerActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import de.hexfieldsstudio.hexfieldsdominion.lobby.LobbyManager;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    @InjectMocks
    private GameController gameController;

    @Mock
    private LobbyManager lobbyManager;

    @Mock
    private GameManager gameManager;

    @Test
    void testLobbyMatchExists() {
        UUID uuid = UUID.randomUUID();
        String lobbyCode = UUID.randomUUID().toString();
        Lobby lobby = mock(Lobby.class);
        when(lobby.getLobbyCode()).thenReturn(lobbyCode);
        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);

        GameController.LobbyCodeResponse response = gameController.lobby(uuid);

        assertEquals(lobbyCode, response.lobbyCode());
    }

    @Test
    void testLobbyMatchDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        when(lobbyManager.findLobbyByMatch(uuid)).thenThrow(new MatchNotFoundException(uuid));

        assertThrowsExactly(MatchNotFoundException.class, () -> gameController.lobby(uuid));
    }

    @Test
    void testFieldsMatchExists() {
        UUID uuid = UUID.randomUUID();
        Lobby lobby = mock(Lobby.class);
        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        List<Field> fields = List.of(
                mock(Field.class),
                mock(Field.class)
        );
        when(gameBoard.getFields()).thenReturn(fields);

        List<Field> fieldsFound = gameController.fields(uuid);

        assertEquals(fields, fieldsFound);
    }

    @Test
    void testFieldsMatchDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        when(lobbyManager.findLobbyByMatch(uuid)).thenThrow(new MatchNotFoundException(uuid));

        assertThrowsExactly(MatchNotFoundException.class, () -> gameController.fields(uuid));
    }

    @Test
    void testRecipes() {
        EnumMap<ResourceType, Integer> recipeTown = new EnumMap<>(ResourceType.class);
        EnumMap<ResourceType, Integer> recipeSettlement = new EnumMap<>(ResourceType.class);
        EnumMap<ResourceType, Integer> recipeStreet = new EnumMap<>(ResourceType.class);

        try (MockedStatic<StructureFactory> structureFactory = mockStatic(StructureFactory.class)) {
            structureFactory.when(() -> StructureFactory.getRecipeForStructureType(StructureType.TOWN)).thenReturn(recipeTown);
            structureFactory.when(() -> StructureFactory.getRecipeForStructureType(StructureType.SETTLEMENT)).thenReturn(recipeSettlement);
            structureFactory.when(() -> StructureFactory.getRecipeForStructureType(StructureType.STREET)).thenReturn(recipeStreet);

            EnumMap<StructureType, EnumMap<ResourceType, Integer>> recipesResult = gameController.recipes();
            assertEquals(recipeTown, recipesResult.get(StructureType.TOWN));
            assertEquals(recipeSettlement, recipesResult.get(StructureType.SETTLEMENT));
            assertEquals(recipeStreet, recipesResult.get(StructureType.STREET));
        }
    }

    @Test
    void testGameEvents() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        SseEmitter expectedEmitter = mock(SseEmitter.class);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);
            when(gameManager.subscribe(uuid, user.getUsername())).thenReturn(expectedEmitter);

            SseEmitter createdEmitter = gameController.gameEvents(uuid);

            assertEquals(expectedEmitter, createdEmitter);
        }
    }

    @Test
    void testRollDice() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);
            gameController.rollDice(uuid);

            verify(gameManager, times(1)).rollDice(uuid, user);
        }
    }

    @Test
    void testEndTurn() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);
            gameController.endTurn(uuid);

            verify(gameManager, times(1)).nextPlayersTurn(uuid, user);
        }
    }

    @Test
    void testGrantedResourcesPresent() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        Map<ResourceType, Integer> grantedResources = new HashMap<>();
        when(gameManager.getGrantedResources(uuid, user)).thenReturn(Optional.of(grantedResources));

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            Map<ResourceType, Integer> result = gameController.grantedResources(uuid, servletResponse);
            assertEquals(result, grantedResources);
        }
    }

    @Test
    void testGrantedResourcesEmpty() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        when(gameManager.getGrantedResources(uuid, user)).thenReturn(Optional.empty());

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            Map<ResourceType, Integer> result = gameController.grantedResources(uuid, servletResponse);
            assertTrue(result.isEmpty());
            verify(servletResponse, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @Test
    void testPlayerAction() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();
        PlayerActionDTO dto = mock(PlayerActionDTO.class);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);
            gameController.playerAction(uuid, dto);

            verify(gameManager, times(1)).handlePlayerAction(uuid, user, dto);
        }
    }

}
