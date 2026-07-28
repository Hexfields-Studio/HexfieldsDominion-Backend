package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Field;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Structure;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import de.hexfieldsstudio.hexfieldsdominion.lobby.LobbyController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MatchTest {

    private Match match;

    private Lobby lobby;

    @BeforeEach
    void setupEach() {
        lobby = mock(Lobby.class);

        match = new Match(
                UUID.randomUUID(),
                LobbyController.BOARD_RADIUS,
                lobby
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void testNextPlayersTurn(int playersCount) {
        List<Player> playersList = new ArrayList<>();
        for (int i = 0; i < playersCount; i++) {
            User user = User.builder()
                    .username("someone" + i)
                    .role(Role.GUEST)
                    .id(i)
                    .build();
            Player player = new Player(user, i);
            playersList.add(player);
        }
        when(lobby.getPlayers()).thenReturn(playersList);

        match = new Match(
                UUID.randomUUID(),
                LobbyController.BOARD_RADIUS,
                lobby
        );

        int[] recordedTurnOrder = new int[playersCount];
        // record
        for (int i = 0; i < playersCount; i++) {
            recordedTurnOrder[i] = match.getPlayers().getPlayerCurrentTurn();
            match.nextPlayersTurn();
        }
        // check against recorded order
        for (int i = 0; i < playersCount; i++) {
            assertEquals(recordedTurnOrder[i], match.getPlayers().getPlayerCurrentTurn());
            match.nextPlayersTurn();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
    void testGrantResourcesForDiceResult(int diceResult) {
        this.createNewMatchWithPlayer(player -> {
            List<Field> fields = match.getGameBoard().getFields();

            Map<ResourceType, Integer> expectedResourcesGranted = new HashMap<>(player.getResources());
            for (Field field : fields) {
                if (field.numberChip() != diceResult) {
                    continue;
                }

                for (Structure structure : match.getGameBoard().getStructures()) {
                    if (structure.getType() != StructureType.SETTLEMENT) {
                        continue;
                    }

                    if (!structure.getPos().contains(field.pos())) {
                        continue;
                    }

                    if (structure.getOwnerId() != player.getPublicId()) {
                        continue;
                    }

                    expectedResourcesGranted.compute(
                            field.resource(),
                            (k, v) -> (v == null) ? 1 : v + 1
                    );
                }
            }

            match.grantResourcesForDiceResult(diceResult);

            assertEquals(expectedResourcesGranted.get(ResourceType.SHEEP), player.getResources().get(ResourceType.SHEEP));
            assertEquals(expectedResourcesGranted.get(ResourceType.WOOD), player.getResources().get(ResourceType.WOOD));
            assertEquals(expectedResourcesGranted.get(ResourceType.WHEAT), player.getResources().get(ResourceType.WHEAT));
            assertEquals(expectedResourcesGranted.get(ResourceType.BRICK), player.getResources().get(ResourceType.BRICK));
        });
    }

    @Test
    void testBuildBuildingUserSuccess() {
        this.createNewMatchWithPlayer(player -> {
            // remove initial structures to prevent conflicts
            match.getGameBoard().getStructures().clear();

            StructureType structureType = StructureType.STREET;
            List<AxialPosition> pos = List.of(new AxialPosition(0, 0), new AxialPosition(0, 1));
            BuildActionDTO dto = new BuildActionDTO(structureType, pos);

            match.buildBuilding(player.getPlayer().getUser(), dto);

            assertEquals(1, match.getGameBoard().getStructures().size());
            assertEquals(structureType, match.getGameBoard().getStructures().getLast().getType());
            assertEquals(pos, match.getGameBoard().getStructures().getLast().getPos());
        });
    }

    @Test
    void testBuildBuildingUserFailNoPlayerForUser() {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();

        // remove initial structures to prevent conflicts
        match.getGameBoard().getStructures().clear();

        StructureType structureType = StructureType.STREET;
        List<AxialPosition> pos = List.of(new AxialPosition(0, 0), new AxialPosition(0, 1));
        BuildActionDTO dto = new BuildActionDTO(structureType, pos);

        match.buildBuilding(user, dto);

        assertTrue(match.getGameBoard().getStructures().isEmpty());
    }

    @Test
    void testBuildBuildingPlayer() {
        // remove initial structures to prevent conflicts
        match.getGameBoard().getStructures().clear();

        StructureType structureType = StructureType.STREET;
        List<AxialPosition> pos = List.of(new AxialPosition(0, 0), new AxialPosition(0, 1));
        BuildActionDTO dto = new BuildActionDTO(structureType, pos);
        PlayerRepresentation player = mock(PlayerRepresentation.class);

        match.buildBuilding(player, dto);

        assertEquals(1, match.getGameBoard().getStructures().size());
        assertEquals(structureType, match.getGameBoard().getStructures().getFirst().getType());
        assertEquals(pos, match.getGameBoard().getStructures().getFirst().getPos());
    }

    @Test
    void testUpgradeSettlementToTownUserSuccess() {
        this.createNewMatchWithPlayer(player -> {
            // remove initial structures to prevent conflicts
            match.getGameBoard().getStructures().clear();

            List<AxialPosition> pos = List.of(new AxialPosition(0, 0), new AxialPosition(0, 1), new AxialPosition(1, 1));
            BuildActionDTO buildDto = new BuildActionDTO(StructureType.SETTLEMENT, pos);
            BuildActionDTO upgradeDto = new BuildActionDTO(StructureType.TOWN, pos);
            match.buildBuilding(player.getPlayer().getUser(), buildDto);

            match.upgradeSettlementToTown(player.getPlayer().getUser(), upgradeDto);

            assertEquals(1, match.getGameBoard().getStructures().size());
            assertEquals(StructureType.TOWN, match.getGameBoard().getStructures().getFirst().getType());
            assertEquals(pos, match.getGameBoard().getStructures().getFirst().getPos());
        });
    }

    @Test
    void testUpgradeSettlementToTownPlayer() {
        // remove initial structures to prevent conflicts
        match.getGameBoard().getStructures().clear();

        List<AxialPosition> pos = List.of(new AxialPosition(0, 0), new AxialPosition(0, 1), new AxialPosition(1, 1));
        BuildActionDTO buildDto = new BuildActionDTO(StructureType.SETTLEMENT, pos);
        BuildActionDTO upgradeDto = new BuildActionDTO(StructureType.TOWN, pos);
        PlayerRepresentation player = mock(PlayerRepresentation.class);
        match.buildBuilding(player, buildDto);

        match.upgradeSettlementToTown(player, upgradeDto);

        assertEquals(1, match.getGameBoard().getStructures().size());
        assertEquals(StructureType.TOWN, match.getGameBoard().getStructures().getFirst().getType());
        assertEquals(pos, match.getGameBoard().getStructures().getFirst().getPos());
    }

    @Test
    void testLetPlayerPayRecipe() {
        this.createNewMatchWithPlayer(player -> {
            // remove initial structures to prevent conflicts
            match.getGameBoard().getStructures().clear();

            match.letPlayerPayRecipe(player.getPlayer().getUser(), new HashMap<>(Map.of(
                    ResourceType.SHEEP, 0,
                    ResourceType.WOOD, 1,
                    ResourceType.WHEAT, 0,
                    ResourceType.BRICK, 1
            )));

            assertEquals(0, player.getResources().get(ResourceType.SHEEP));
            assertEquals(-1, player.getResources().get(ResourceType.WOOD));
            assertEquals(0, player.getResources().get(ResourceType.WHEAT));
            assertEquals(-1, player.getResources().get(ResourceType.BRICK));
        });
    }

    private void createNewMatchWithPlayer(Consumer<PlayerRepresentation> consumer) {
        User user = User.builder()
                .username("someone")
                .role(Role.GUEST)
                .build();
        when(lobby.getPlayers()).thenReturn(new ArrayList<>(List.of(
                new Player(user, 0)
        )));

        match = new Match(
                UUID.randomUUID(),
                LobbyController.BOARD_RADIUS,
                lobby
        );

        Optional<PlayerRepresentation> playerOptional = match.getPlayers().getPlayerForUser(user);
        assert playerOptional.isPresent();
        PlayerRepresentation player = playerOptional.get();
        player.getResources().putAll(Map.of(
                ResourceType.SHEEP, 0,
                ResourceType.WOOD, 0,
                ResourceType.WHEAT, 0,
                ResourceType.BRICK, 0
        ));

        consumer.accept(player);
    }

}
