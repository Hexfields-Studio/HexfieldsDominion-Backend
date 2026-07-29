package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.error.ForbiddenException;
import de.hexfieldsstudio.hexfieldsdominion.game.board.GameBoard;
import de.hexfieldsstudio.hexfieldsdominion.game.board.StructureFactory;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PlayerActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradePlayerDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.InvalidBuildRequestException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MoveHasntBeenImplementedException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotPlayersTurnException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.GamePlayers;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.trading.TradingHandler;
import de.hexfieldsstudio.hexfieldsdominion.game.types.PlayerActionType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import de.hexfieldsstudio.hexfieldsdominion.lobby.LobbyManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameManagerTest {

    private static final int POINTS_REQUIRED_TO_WIN = 10;

    @InjectMocks
    private GameManager gameManager;

    @Mock
    private LobbyManager lobbyManager;

    @Test
    void testRollDice() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        when(players.isPlayersTurn(user)).thenReturn(true);
        when(match.isRolledDiceThisTurn()).thenReturn(false);

        gameManager.rollDice(uuid, user);

        verify(match, times(1)).setRolledDiceThisTurn(anyBoolean());
        verify(match).setRolledDiceThisTurn(true);
        verify(match, times(1)).setCurrentDiceResult(any());
        verify(match, times(1)).grantResourcesForDiceResult(anyInt());
    }

    @Test
    void testRollDiceNotPlayersTurn() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        this.testNotPlayersTurn(uuid, user, () -> gameManager.rollDice(uuid, user));
    }

    private void testNotPlayersTurn(UUID uuid, User user, Runnable action) {
        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        when(players.isPlayersTurn(user)).thenReturn(false);

        assertThrowsExactly(NotPlayersTurnException.class, action::run);
    }

    @Test
    void testRollDiceAlreadyRolledThisTurn() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        when(players.isPlayersTurn(user)).thenReturn(true);
        when(match.isRolledDiceThisTurn()).thenReturn(true);

        Exception exception = assertThrowsExactly(ForbiddenException.class, () -> gameManager.rollDice(uuid, user));
        assertEquals("Already rolled dice.", exception.getMessage());
    }

    @Test
    void testNextPlayersTurn() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());
        TradingHandler tradingHandler = mock(TradingHandler.class);
        when(match.getTradingHandler()).thenReturn(tradingHandler);

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        when(players.isPlayersTurn(user)).thenReturn(true);

        gameManager.nextPlayersTurn(uuid, user);

        verify(match, times(1)).nextPlayersTurn();
    }

    @Test
    void testNextPlayersTurnNotPlayersTurn() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        this.testNotPlayersTurn(uuid, user, () -> gameManager.nextPlayersTurn(uuid, user));
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void testAddPoints(int pointsToAdd) {
        this.verifyNoWinnerAfterAddPoints(0, pointsToAdd);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void testAddPointsWhenPlayerAlreadyHasPoints(int pointsToAdd) {
        int initialPoints = 1;
        this.verifyNoWinnerAfterAddPoints(initialPoints, pointsToAdd);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void testAddPointsXUntilWin(int pointsUntilWin) {
        this.verifyNoWinnerAfterAddPoints(0, POINTS_REQUIRED_TO_WIN - pointsUntilWin);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void testAddPointsXMoreThanWin(int pointsMoreThanWin) {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        Player player = new Player(user, 0);
        PlayerRepresentation playerRepresentation = new PlayerRepresentation(player);

        Match match = mock(Match.class);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        when(players.getPlayerForUser(user)).thenReturn(Optional.of(playerRepresentation));

        gameManager.addPoints(match, user, POINTS_REQUIRED_TO_WIN + pointsMoreThanWin);

        verify(players, times(1)).setWinner(any());
        verify(players).setWinner(playerRepresentation);
        assertEquals(POINTS_REQUIRED_TO_WIN + pointsMoreThanWin, playerRepresentation.getPoints());
    }

    @Test
    void testAddPointsNoPlayerForUser() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();

        Match match = mock(Match.class);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        when(players.getPlayerForUser(user)).thenReturn(Optional.empty());

        gameManager.addPoints(match, user, POINTS_REQUIRED_TO_WIN);

        verify(players, times(0)).setWinner(any());
    }

    private void verifyNoWinnerAfterAddPoints(int initialPoints, int pointsToAdd) {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        Player player = new Player(user, 0);
        PlayerRepresentation playerRepresentation = new PlayerRepresentation(player);
        playerRepresentation.setPoints(initialPoints);

        Match match = mock(Match.class);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        when(players.getPlayerForUser(user)).thenReturn(Optional.of(playerRepresentation));

        gameManager.addPoints(match, user, pointsToAdd);

        verify(players, times(0)).setWinner(any());
        assertEquals(initialPoints + pointsToAdd, playerRepresentation.getPoints());
    }

    @Test
    void testHandlePlayerActionBuild() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();
        BuildActionDTO dto = new BuildActionDTO(
                StructureType.STREET,
                List.of(AxialPosition.of(0, 0), AxialPosition.of(0, 1))
        );
        // is normally done automatically when a subtype is created for a received PlayerAction
        dto.setType(PlayerActionType.BUILD);

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        when(players.isPlayersTurn(user)).thenReturn(true);

        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        when(match.getValidator()).thenReturn(validator);

        when(validator.validate(user, match, dto)).thenReturn(true);

        gameManager.handlePlayerAction(uuid, user, dto);

        this.verifyBuildBuildingStreet(match, user, dto);
    }

    @Test
    void testHandlePlayerActionBuildNotPlayersTurn() {
        this.testHandlePlayerActionNotPlayersTurn(PlayerActionType.BUILD);
    }

    @Test
    void testHandlePlayerActionTradeBank() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        TradeBankDTO dto = mock(TradeBankDTO.class);
        when(dto.getType()).thenReturn(PlayerActionType.TRADE_BANK);

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());
        TradingHandler tradingHandler = mock(TradingHandler.class);
        when(match.getTradingHandler()).thenReturn(tradingHandler);

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        when(players.isPlayersTurn(user)).thenReturn(true);

        gameManager.handlePlayerAction(uuid, user, dto);

        verify(tradingHandler, times(1)).tradeBank(any(), any(), any());
        verify(tradingHandler).tradeBank(user, match, dto);
    }

    @Test
    void testHandlePlayerActionTradeBankNotPlayersTurn() {
        this.testHandlePlayerActionNotPlayersTurn(PlayerActionType.TRADE_BANK);
    }

    @Test
    void testHandlePlayerActionTradePlayer() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        TradePlayerDTO dto = mock(TradePlayerDTO.class);
        when(dto.getType()).thenReturn(PlayerActionType.TRADE_PLAYER);

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());
        TradingHandler tradingHandler = mock(TradingHandler.class);
        when(match.getTradingHandler()).thenReturn(tradingHandler);

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);

        gameManager.handlePlayerAction(uuid, user, dto);

        verify(tradingHandler, times(1)).handlePlayerTrade(any(), any(), any());
        verify(tradingHandler).handlePlayerTrade(user, match, dto);
    }

    private void testHandlePlayerActionNotPlayersTurn(PlayerActionType playerActionType) {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        PlayerActionDTO dto = mock(PlayerActionDTO.class);
        when(dto.getType()).thenReturn(playerActionType);

        this.testNotPlayersTurn(uuid, user, () -> gameManager.handlePlayerAction(uuid, user, dto));
    }

    //TODO: fix. Geht aus irgendnem Grund nicht wenn man alle Test in der Klasse auf einmal ausführt
    //@Test
    void testHandlePlayerActionNotImplemented() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        UUID uuid = UUID.randomUUID();

        try (MockedStatic<PlayerActionType> mockedStatic = mockStatic(PlayerActionType.class)) {
            PlayerActionType[] mockedActionTypes = new PlayerActionType[]{
                    PlayerActionType.BUILD,
                    PlayerActionType.PICK_DICE_PAIR,
                    PlayerActionType.TRADE_BANK,
                    PlayerActionType.TRADE_PLAYER,
                    // additional entry so there is one index left to use
                    PlayerActionType.TRADE_PLAYER,
            };
            mockedStatic.when(PlayerActionType::values).thenReturn(mockedActionTypes);

            PlayerActionType playerActionTypeUnknown = mock(PlayerActionType.class);
            // use last index left
            when(playerActionTypeUnknown.ordinal()).thenReturn(mockedActionTypes.length - 1);

            PlayerActionDTO dto = mock(PlayerActionDTO.class);
            when(dto.getType()).thenReturn(playerActionTypeUnknown);

            assertThrowsExactly(MoveHasntBeenImplementedException.class, () -> gameManager.handlePlayerAction(uuid, user, dto));
        }
    }

    @Test
    void testBuildBuildingStreet() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        BuildActionDTO dto = new BuildActionDTO(
                StructureType.STREET,
                List.of(AxialPosition.of(0, 0), AxialPosition.of(0, 1))
        );

        Match match = mock(Match.class);

        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        when(match.getValidator()).thenReturn(validator);

        when(validator.validate(user, match, dto)).thenReturn(true);

        gameManager.buildBuilding(user, match, dto);

        this.verifyBuildBuildingStreet(match, user, dto);
    }

    void verifyBuildBuildingStreet(Match match, User user, BuildActionDTO dto) {
        verify(match, times(1)).buildBuilding(any(User.class), any(BuildActionDTO.class));
        verify(match).buildBuilding(user, dto);
        verify(match, times(1)).letPlayerPayRecipe(any(User.class), any());
        verify(match).letPlayerPayRecipe(user, StructureFactory.getRecipeForStructureType(dto.getStructureType()));
    }

    @Test
    void testBuildBuildingSettlement() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        BuildActionDTO dto = new BuildActionDTO(
                StructureType.SETTLEMENT,
                List.of(AxialPosition.of(0, 0), AxialPosition.of(0, 1), AxialPosition.of(1, 1))
        );

        PlayerRepresentation player = mock(PlayerRepresentation.class);
        Match match = mock(Match.class);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        when(players.getPlayerForUser(user)).thenReturn(Optional.of(player));
        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        when(match.getValidator()).thenReturn(validator);

        when(validator.validate(user, match, dto)).thenReturn(true);

        gameManager.buildBuilding(user, match, dto);

        verify(match, times(1)).buildBuilding(any(User.class), any(BuildActionDTO.class));
        verify(match).buildBuilding(user, dto);
        verify(match, times(1)).letPlayerPayRecipe(any(User.class), any());
        verify(match).letPlayerPayRecipe(user, StructureFactory.getRecipeForStructureType(dto.getStructureType()));
        verify(player, times(1)).addPoints(anyInt());
        verify(player).addPoints(1);
    }

    @Test
    void testBuildBuildingTown() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        BuildActionDTO dto = new BuildActionDTO(
                StructureType.TOWN,
                List.of(AxialPosition.of(0, 0), AxialPosition.of(0, 1), AxialPosition.of(1, 1))
        );

        PlayerRepresentation player = mock(PlayerRepresentation.class);
        Match match = mock(Match.class);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        when(players.getPlayerForUser(user)).thenReturn(Optional.of(player));
        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        when(match.getValidator()).thenReturn(validator);

        when(validator.validate(user, match, dto)).thenReturn(true);

        gameManager.buildBuilding(user, match, dto);

        verify(match, times(1)).upgradeSettlementToTown(any(User.class), any(BuildActionDTO.class));
        verify(match).upgradeSettlementToTown(user, dto);
        verify(match, times(1)).letPlayerPayRecipe(any(User.class), any());
        verify(match).letPlayerPayRecipe(user, StructureFactory.getRecipeForStructureType(dto.getStructureType()));
        verify(player, times(1)).addPoints(anyInt());
        verify(player).addPoints(2);
    }

    @Test
    void testBuildBuildingInvalidBuildRequest() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        BuildActionDTO dto = new BuildActionDTO(
                StructureType.STREET,
                List.of(AxialPosition.of(0, 0), AxialPosition.of(0, 1))
        );

        Match match = mock(Match.class);
        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        when(match.getValidator()).thenReturn(validator);

        when(validator.validate(user, match, dto)).thenReturn(false);

        assertThrowsExactly(InvalidBuildRequestException.class, () -> gameManager.buildBuilding(user, match, dto));
    }

    @Test
    void testGetGrantedResources() {
        UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        Player player = new Player(user, 0);
        PlayerRepresentation playerRepresentation = new PlayerRepresentation(player);
        Map<ResourceType, Integer> grantedResourcesPlayer = mock(Map.class);

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        Map<Integer, Map<ResourceType, Integer>> grantedResources = mock(Map.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());
        TradingHandler tradingHandler = mock(TradingHandler.class);
        when(match.getTradingHandler()).thenReturn(tradingHandler);

        when(players.getPlayerForUser(user)).thenReturn(Optional.of(playerRepresentation));
        when(match.getGrantedResourcesThisTurn()).thenReturn(grantedResources);
        when(grantedResources.get(playerRepresentation.getPublicId())).thenReturn(grantedResourcesPlayer);
        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        gameManager.subscribe(uuid, user.getUsername());

        Optional<Map<ResourceType, Integer>> result = gameManager.getGrantedResources(uuid, user);

        assertTrue(result.isPresent());
        assertEquals(grantedResourcesPlayer, result.get());
    }

    @Test
    void testGetGrantedResourcesPlayerNotFound() {
        UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());
        TradingHandler tradingHandler = mock(TradingHandler.class);
        when(match.getTradingHandler()).thenReturn(tradingHandler);

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);
        gameManager.subscribe(uuid, user.getUsername());

        Optional<Map<ResourceType, Integer>> result = gameManager.getGrantedResources(uuid, user);

        assertFalse(result.isPresent());
    }

    @Test
    void testSubscribeLobbyFound() {
        UUID uuid = UUID.randomUUID();

        Lobby lobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(lobby.getMatch()).thenReturn(match);
        GamePlayers players = mock(GamePlayers.class);
        when(match.getPlayers()).thenReturn(players);
        GameBoard gameBoard = mock(GameBoard.class);
        when(match.getGameBoard()).thenReturn(gameBoard);
        when(gameBoard.getStructures()).thenReturn(new ArrayList<>());
        TradingHandler tradingHandler = mock(TradingHandler.class);
        when(match.getTradingHandler()).thenReturn(tradingHandler);

        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);

        SseEmitter emitter = gameManager.subscribe(uuid, "someone");

        assertNotNull(emitter);
    }

    @Test
    void testSubscribeLobbyNotFound() {
        UUID uuid = UUID.randomUUID();
        when(lobbyManager.findLobbyByMatch(uuid)).thenThrow(MatchNotFoundException.class);

        assertThrowsExactly(MatchNotFoundException.class, () -> gameManager.subscribe(uuid, "someone"));
    }

    @Test
    void testOnUnsubscribe() {
        UUID uuid = UUID.randomUUID();
        AppConfig appConfig = mock(AppConfig.class);
        Lobby lobby = new Lobby(appConfig);
        Player player = mock(Player.class);
        lobby.getPlayers().add(player);

        when(player.getUsername()).thenReturn("someone");
        when(lobbyManager.findLobbyByMatch(uuid)).thenReturn(lobby);

        gameManager.onUnsubscribe(uuid, "someone");

        assertTrue(lobby.getPlayers().isEmpty());
    }

}
