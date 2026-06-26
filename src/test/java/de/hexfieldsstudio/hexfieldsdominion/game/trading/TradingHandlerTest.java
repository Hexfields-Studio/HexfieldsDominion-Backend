package de.hexfieldsstudio.hexfieldsdominion.game.trading;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradePlayerDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotEnoughResourcesException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.GamePlayers;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.TradingStatus;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TradingHandlerTest {

    private TradingHandler tradingHandler;

    @BeforeEach
    public void setUp() {
        tradingHandler = spy(new TradingHandler());
    }

    @ParameterizedTest
    @ArgumentsSource(TradingStatusProvider.AllProvider.class)
    void testHandleVerifyCalled(TradingStatus status) {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withPlayerTarget(tradingHandler, false, status);
        User user = objects.getUser();
        Match match = objects.getMatch();
        TradePlayerDTO tradePlayerDTO = objects.getTradePlayerDTO();
        TradePlayerDTO existingTradeDto = objects.getExistingTradeDto();

        // we don't want to call createTrade because of call count
        tradingHandler.getPlayerTrades().put(0, new PlayerTrade(
                existingTradeDto.getId(),
                null,
                existingTradeDto.getStatus(),
                existingTradeDto.getTarget(),
                0,
                existingTradeDto.getOffered(),
                existingTradeDto.getRequested()
        ));

        int expectedTimesCreate = status == TradingStatus.OFFERED ? 1 : 0;
        int expectedTimesEdit = status == TradingStatus.CHANGED ? 1 : 0;
        int expectedTimesAccept = status == TradingStatus.ACCEPTED ? 1 : 0;
        int expectedTimesDeny = status == TradingStatus.DENIED ? 1 : 0;
        int expectedTimesCancel = status == TradingStatus.CANCELLED ? 1 : 0;

        tradingHandler.handlePlayerTrade(user, match, tradePlayerDTO);

        verify(tradingHandler, times(expectedTimesCreate)).createTrade(
                expectedTimesCreate == 0 ? any() : user,
                expectedTimesCreate == 0 ? any() : match,
                expectedTimesCreate == 0 ? any() : tradePlayerDTO
        );
        verify(tradingHandler, times(expectedTimesEdit)).editTrade(
                expectedTimesEdit == 0 ? any() : user,
                expectedTimesEdit == 0 ? any() : match,
                expectedTimesEdit == 0 ? any() : tradePlayerDTO
        );
        verify(tradingHandler, times(expectedTimesAccept)).acceptTrade(
                expectedTimesAccept == 0 ? any() : user,
                expectedTimesAccept == 0 ? any() : match,
                expectedTimesAccept == 0 ? any() : tradePlayerDTO
        );
        verify(tradingHandler, times(expectedTimesDeny)).denyTrade(
                expectedTimesDeny == 0 ? any() : tradePlayerDTO
        );
        verify(tradingHandler, times(expectedTimesCancel)).cancelTrade(
                expectedTimesCancel == 0 ? any() : tradePlayerDTO
        );
    }

    @Test
    void testCreateTradeNoPlayerForUser() {
        User user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        Match match = MockObjects.match(gamePlayers);

        User userNoPlayer = MockObjects.user("testNoPlayer");

        TradePlayerDTO tradePlayerDTO = mock(TradePlayerDTO.class);

        tradingHandler.createTrade(userNoPlayer, match, tradePlayerDTO);

        assertTrue(tradingHandler.getPlayerTrades().isEmpty());
    }

    @Test
    void testCreateTrade() {
        User user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        Match match = MockObjects.match(gamePlayers);

        TradePlayerDTO tradePlayerDTO = MockObjects.createTradeDto(player);

        tradingHandler.createTrade(user, match, tradePlayerDTO);

        assertFalse(tradingHandler.getPlayerTrades().isEmpty());
        PlayerTrade trade = tradingHandler.getPlayerTrades().get(0);
        assertEquals(tradePlayerDTO.getStatus(), trade.getStatus());
        assertEquals(tradePlayerDTO.getTarget(), trade.getTarget());
        assertEquals(tradePlayerDTO.getOffered(), trade.getOffered());
        assertEquals(tradePlayerDTO.getRequested(), trade.getRequested());
    }

    @Test
    void testCreateTradeWithExistingTrade() {
        User user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        Match match = MockObjects.match(gamePlayers);

        TradePlayerDTO tradePlayerDTO = MockObjects.createTradeDto(player);

        tradingHandler.createTrade(user, match, tradePlayerDTO);
        tradingHandler.createTrade(user, match, tradePlayerDTO);

        assertEquals(2, tradingHandler.getPlayerTrades().size());
        assertEquals(2, tradingHandler.getPlayerTrades().keySet().stream().distinct().count());
        PlayerTrade trade = tradingHandler.getPlayerTrades().get(1);
        assertEquals(tradePlayerDTO.getStatus(), trade.getStatus());
        assertEquals(tradePlayerDTO.getTarget(), trade.getTarget());
        assertEquals(tradePlayerDTO.getOffered(), trade.getOffered());
        assertEquals(tradePlayerDTO.getRequested(), trade.getRequested());
    }

    @Test
    void testEditTradeSingleTarget() {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withPlayerTarget(tradingHandler, true, TradingStatus.CHANGED);
        User user = objects.getUser();

        this.testEditTradeForObjects(objects, user, false, 0, false);
        this.testEditTradeForObjects(objects, user, false, 1, true);
    }

    @Test
    void testEditTradeAllPlayersTarget() {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withAllPlayersTarget(tradingHandler, true, TradingStatus.CHANGED);
        User user = objects.getUser();

        this.testEditTradeForObjects(objects, user, true, 0, false);
        this.testEditTradeForObjects(objects, user, false, 1, false);
    }

    @Test
    void testEditTradeAllPlayersTargetNoPlayerForUser() {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withAllPlayersTarget(tradingHandler, true, TradingStatus.CHANGED);

        User userNoPlayer = MockObjects.user("testNoPlayer");

        this.testEditTradeForObjects(objects, userNoPlayer, true, 0, false);
        this.testEditTradeForObjects(objects, userNoPlayer, false, 1, true);
    }

    private void testEditTradeForObjects(TradePlayerDtoObjects objects, User editingUser, boolean expectValuesBefore, int tradeIdToCheck, boolean expectTradeDoesNotExist) {
        Match match = objects.getMatch();

        TradePlayerDTO tradePlayerDTO = objects.getTradePlayerDTO();
        when(tradePlayerDTO.getOffered()).thenReturn(Map.of(ResourceType.SHEEP, 2));
        when(tradePlayerDTO.getOffered()).thenReturn(Map.of(ResourceType.WHEAT, 1));

        PlayerTrade tradeBefore = tradingHandler.getPlayerTrades().get(0);
        TradingStatus statusBefore = tradeBefore.getStatus();
        Map<ResourceType, Integer> offeredBefore = tradeBefore.getOffered();
        Map<ResourceType, Integer> requestedBefore = tradeBefore.getRequested();

        TradingStatus expectedStatus = expectValuesBefore ? statusBefore : tradePlayerDTO.getStatus();
        Map<ResourceType, Integer> expectedOffered = expectValuesBefore ? offeredBefore : tradePlayerDTO.getOffered();
        Map<ResourceType, Integer> expectedRequested = expectValuesBefore ? requestedBefore : tradePlayerDTO.getRequested();

        tradingHandler.editTrade(editingUser, match, tradePlayerDTO);

        PlayerTrade tradeToCheck = tradingHandler.getPlayerTrades().get(tradeIdToCheck);
        if (expectTradeDoesNotExist) {
            assertNull(tradeToCheck);
            return;
        }
        assertNotNull(tradeToCheck);
        assertEquals(expectedStatus, tradeToCheck.getStatus());
        assertEquals(expectedOffered, tradeToCheck.getOffered());
        assertEquals(expectedRequested, tradeToCheck.getRequested());
    }

    //TODO: accept? Oder zu aufwendig für den Moment?

    @ParameterizedTest
    @ArgumentsSource(TradingStatusProvider.AllProvider.class)
    void testDenyTradeAllStatusSingleTarget(TradingStatus tradingStatus) {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withPlayerTarget(tradingHandler, true, TradingStatus.DENIED);
        TradePlayerDTO tradePlayerDTO = objects.getTradePlayerDTO();

        PlayerTrade trade = tradingHandler.getPlayerTrades().get(0);
        trade.setStatus(tradingStatus);

        tradingHandler.denyTrade(tradePlayerDTO);

        assertEquals(TradingStatus.DENIED, trade.getStatus());
    }

    @ParameterizedTest
    @ArgumentsSource(TradingStatusProvider.AllButOfferedProvider.class)
    void testDenyTradeAllStatusWithoutOfferedAllPlayersTarget(TradingStatus tradingStatus) {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withAllPlayersTarget(tradingHandler, true, TradingStatus.DENIED);
        TradePlayerDTO tradePlayerDTO = objects.getTradePlayerDTO();

        PlayerTrade trade = tradingHandler.getPlayerTrades().get(0);
        trade.setStatus(tradingStatus);

        tradingHandler.denyTrade(tradePlayerDTO);

        assertEquals(TradingStatus.DENIED, trade.getStatus());
    }

    @Test
    void testDenyTradeOfferedToAllPlayers() {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withAllPlayersTarget(tradingHandler, true, TradingStatus.DENIED);
        TradePlayerDTO tradePlayerDTO = objects.getTradePlayerDTO();

        TradingStatus statusBefore = TradingStatus.OFFERED;
        PlayerTrade trade = tradingHandler.getPlayerTrades().get(0);
        trade.setStatus(statusBefore);

        tradingHandler.denyTrade(tradePlayerDTO);

        assertEquals(statusBefore, trade.getStatus());
    }

    @Test
    void testCancelTrade() {
        TradePlayerDtoObjects objects = TradePlayerDtoObjects.withPlayerTarget(tradingHandler, true, TradingStatus.CANCELLED);
        TradePlayerDTO tradePlayerDTO = objects.getTradePlayerDTO();

        tradingHandler.cancelTrade(tradePlayerDTO);

        assertEquals(TradingStatus.CANCELLED, tradingHandler.getPlayerTrades().get(0).getStatus());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testTradeBank(boolean alreadyOwnsRequestedResource) {
        User user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        Match match = MockObjects.match(gamePlayers);

        Map<ResourceType, Integer> resourcesBefore = alreadyOwnsRequestedResource ? Map.of(
                ResourceType.WHEAT, 10,
                ResourceType.WOOD, 10,
                ResourceType.SHEEP, 10,
                ResourceType.BRICK, 10
        ) : Map.of(
                ResourceType.WHEAT, 10,
                ResourceType.SHEEP, 10,
                ResourceType.BRICK, 10
        );
        player.getResources().putAll(resourcesBefore);

        int amountRequested = 1;
        int amountOffered = amountRequested * TradingHandler.GIVE_GET_RATIO;
        TradeBankDTO tradeBankDTO = MockObjects.tradeBankDto(amountRequested, amountOffered);

        tradingHandler.tradeBank(user, match, tradeBankDTO);

        assertEquals(resourcesBefore.get(ResourceType.WHEAT),
                player.getResources().get(ResourceType.WHEAT));
        if (alreadyOwnsRequestedResource) {
            assertEquals(resourcesBefore.get(ResourceType.WOOD) + amountRequested,
                    player.getResources().get(ResourceType.WOOD));
        } else {
            assertEquals(amountRequested, player.getResources().get(ResourceType.WOOD));
        }
        assertEquals(resourcesBefore.get(ResourceType.SHEEP) - amountOffered,
                player.getResources().get(ResourceType.SHEEP));
        assertEquals(resourcesBefore.get(ResourceType.BRICK),
                player.getResources().get(ResourceType.BRICK));
    }

    @Test
    void testTradeBankNoPlayerForUser() {
        User user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        Match match = MockObjects.match(gamePlayers);

        Map<ResourceType, Integer> resourcesBefore = Map.of(
                ResourceType.WHEAT, 10,
                ResourceType.WOOD, 10,
                ResourceType.SHEEP, 10,
                ResourceType.BRICK, 10
        );
        player.getResources().putAll(resourcesBefore);

        User userNoPlayer = MockObjects.user("testNoPlayer");

        int amountRequested = 1;
        int amountOffered = amountRequested * TradingHandler.GIVE_GET_RATIO;
        TradeBankDTO tradeBankDTO = MockObjects.tradeBankDto(amountRequested, amountOffered);

        tradingHandler.tradeBank(userNoPlayer, match, tradeBankDTO);

        resourcesBefore.forEach((resource, amountBefore) -> {
            assertEquals(amountBefore, player.getResources().get(resource));
        });
    }

    @Test
    void testTradeBankInvalidRatio() {
        User user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        Match match = MockObjects.match(gamePlayers);

        int amountRequested = 1;
        int amountOffered = 2;
        TradeBankDTO tradeBankDTO = MockObjects.tradeBankDto(amountRequested, amountOffered);

        assertThrows(BadRequestException.class, () -> tradingHandler.tradeBank(user, match, tradeBankDTO));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testTradeBankNotEnoughResources(boolean offeredResourceAlreadyStored) {
        User user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        Match match = MockObjects.match(gamePlayers);

        Map<ResourceType, Integer> resourcesBefore = offeredResourceAlreadyStored ? Map.of(
                ResourceType.WHEAT, 10,
                ResourceType.WOOD, 10,
                ResourceType.SHEEP, 0,
                ResourceType.BRICK, 10
        ) : Map.of(
                ResourceType.WHEAT, 10,
                ResourceType.WOOD, 10,
                ResourceType.BRICK, 10
        );
        player.getResources().putAll(resourcesBefore);

        int amountRequested = 1;
        int amountOffered = amountRequested * TradingHandler.GIVE_GET_RATIO;
        TradeBankDTO tradeBankDTO = MockObjects.tradeBankDto(amountRequested, amountOffered);

        assertThrows(NotEnoughResourcesException.class, () -> tradingHandler.tradeBank(user, match, tradeBankDTO));
    }

    @Test
    void testClearTrades() {
        PlayerTrade existingTrade = new PlayerTrade(
                0,
                null,
                TradingStatus.OFFERED,
                TradingTarget.ofPlayer(0),
                0,
                emptyMap(),
                emptyMap()
        );
        tradingHandler.getPlayerTrades().put(0, existingTrade);

        tradingHandler.clearTrades();

        assertTrue(tradingHandler.getPlayerTrades().isEmpty());
    }

    static class TradingStatusProvider {
        static class AllProvider implements ArgumentsProvider {
            @Override
            @NullMarked
            public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
                return Arrays.stream(TradingStatus.values())
                        .map(Arguments::of);
            }
        }
        static class AllButOfferedProvider implements ArgumentsProvider {
            @Override
            @NullMarked
            public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
                return Arrays.stream(TradingStatus.values())
                        .filter(tradingStatus -> tradingStatus != TradingStatus.OFFERED)
                        .map(Arguments::of);
            }
        }
    }

}

class MockObjects {
    static User user() {
        return user("test");
    }
    static User user(String username) {
        return User.builder()
                .username(username)
                .role(Role.PLAYER)
                .build();
    }

    static PlayerRepresentation player(User user) {
        return new PlayerRepresentation(new Player(user, 0));
    }

    static GamePlayers gamePlayers(PlayerRepresentation player) {
        GamePlayers gamePlayers = mock(GamePlayers.class);
        when(gamePlayers.getPlayerForUser(player.getPlayer().getUser())).thenReturn(Optional.of(player));
        return gamePlayers;
    }

    static Match match(GamePlayers gamePlayers) {
        Match match = mock(Match.class);
        when(match.getPlayers()).thenReturn(gamePlayers);
        return match;
    }

    static TradeBankDTO tradeBankDto(int amountRequested, int amountOffered) {
        TradeBankDTO tradeBankDTO = mock(TradeBankDTO.class);
        when(tradeBankDTO.getResourceRequested()).thenReturn(ResourceType.WOOD);
        when(tradeBankDTO.getAmountRequested()).thenReturn(amountRequested);
        when(tradeBankDTO.getResourceOffered()).thenReturn(ResourceType.SHEEP);
        when(tradeBankDTO.getAmountOffered()).thenReturn(amountOffered);
        return tradeBankDTO;
    }

    static TradePlayerDTO createTradeDto(PlayerRepresentation player) {
        TradePlayerDTO tradePlayerDTO = mock(TradePlayerDTO.class);
        when(tradePlayerDTO.getStatus()).thenReturn(TradingStatus.OFFERED);
        when(tradePlayerDTO.getTarget()).thenReturn(TradingTarget.ofPlayer(player.getPublicId()));
        when(tradePlayerDTO.getOffered()).thenReturn(emptyMap());
        when(tradePlayerDTO.getRequested()).thenReturn(emptyMap());
        return tradePlayerDTO;
    }
}

class TradePlayerDtoObjects {
    private final User user;
    private final Match match;
    private final TradePlayerDTO tradePlayerDTO;
    private final TradePlayerDTO existingTradeDto;

    public TradePlayerDtoObjects(TradingHandler tradingHandler, boolean createExistingTrade, TradingStatus tradingStatus, boolean allPlayers) {
        this.user = MockObjects.user();
        PlayerRepresentation player = MockObjects.player(user);
        GamePlayers gamePlayers = MockObjects.gamePlayers(player);
        this.match = MockObjects.match(gamePlayers);

        this.existingTradeDto = new TradePlayerDTO(
                0,
                TradingStatus.OFFERED,
                allPlayers ? new TradingTarget(true, null) : TradingTarget.ofPlayer(player.getPublicId()),
                emptyMap(),
                emptyMap()
        );
        if (createExistingTrade) {
            tradingHandler.createTrade(user, match, existingTradeDto);
        }

        this.tradePlayerDTO = mock(TradePlayerDTO.class);
        when(tradePlayerDTO.getStatus()).thenReturn(tradingStatus);
        when(tradePlayerDTO.getId()).thenReturn(existingTradeDto.getId());
    }

    public static TradePlayerDtoObjects withPlayerTarget(TradingHandler tradingHandler, boolean createExistingTrade, TradingStatus tradingStatus) {
        return new TradePlayerDtoObjects(tradingHandler, createExistingTrade, tradingStatus, false);
    }

    public static TradePlayerDtoObjects withAllPlayersTarget(TradingHandler tradingHandler, boolean createExistingTrade, TradingStatus tradingStatus) {
        return new TradePlayerDtoObjects(tradingHandler, createExistingTrade, tradingStatus, true);
    }

    public User getUser() {
        return user;
    }

    public Match getMatch() {
        return match;
    }

    public TradePlayerDTO getTradePlayerDTO() {
        return tradePlayerDTO;
    }

    public TradePlayerDTO getExistingTradeDto() {
        return existingTradeDto;
    }
}