package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.BuildingABuildingValidator;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.TooLittleSpaceException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.GamePlayers;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class StructureFactoryTest {

    private static final List<AxialPosition> CORNER = List.of(
            AxialPosition.of(0, 0),
            AxialPosition.of(1, -1),
            AxialPosition.of(1, 0)
    );
    private static final List<Field> RESOURCE_FIELDS = List.of(
            new Field(AxialPosition.of(0, 0), 5, ResourceType.WOOD),
            new Field(AxialPosition.of(1, -1), 8, ResourceType.WHEAT),
            new Field(AxialPosition.of(1, 0), 10, ResourceType.SHEEP)
    );

    @ParameterizedTest
    @MethodSource("recipes")
    void returnsTheRecipeForEveryStructureType(StructureType type, Map<ResourceType, Integer> expectedRecipe) {
        assertEquals(expectedRecipe, StructureFactory.getRecipeForStructureType(type));
    }

    @ParameterizedTest
    @MethodSource("recipes")
    void buildsAStructureFromThePlayerAndDto(StructureType type, Map<ResourceType, Integer> expectedRecipe) {
        PlayerRepresentation player = mock(PlayerRepresentation.class);
        when(player.getPublicId()).thenReturn(42);
        List<AxialPosition> positions = type == StructureType.STREET
                ? CORNER.subList(0, 2)
                : CORNER;
        BuildActionDTO dto = new BuildActionDTO(type, positions);

        Structure structure = StructureFactory.buildStructureFromDTO(player, dto);

        assertAll(
                () -> assertEquals(type, structure.getType()),
                () -> assertEquals(positions, structure.getPos()),
                () -> assertEquals(42, structure.getOwnerId()),
                () -> assertEquals(expectedRecipe, structure.getRecipe())
        );
    }

    @Test
    void randomlyBuildsTwoSettlementsWithAStreetForEveryPlayer() throws GameBoard.NotAllFieldsFoundException {
        Match match = mock(Match.class);
        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        GameBoard gameBoard = mock(GameBoard.class);
        GamePlayers gamePlayers = mock(GamePlayers.class);
        PlayerRepresentation firstPlayer = playerWithId(10);
        PlayerRepresentation secondPlayer = playerWithId(20);
        List<Structure> structures = new ArrayList<>();

        when(match.getGameBoard()).thenReturn(gameBoard);
        when(match.getPlayers()).thenReturn(gamePlayers);
        when(gamePlayers.getPlayers()).thenReturn(List.of(firstPlayer, secondPlayer));
        when(gameBoard.getStructures()).thenReturn(structures);
        when(gameBoard.getFieldsAt(anyList())).thenReturn(RESOURCE_FIELDS);
        when(validator.getCorners()).thenReturn(Set.of(CORNER));
        when(validator.validate(isNull(), same(match), any(BuildActionDTO.class))).thenReturn(true);

        StructureFactory.randomlyBuildInitialStructures(match, validator);

        assertEquals(8, structures.size());
        assertInitialStructuresForPlayer(structures, 10);
        assertInitialStructuresForPlayer(structures, 20);
        verify(validator, times(8)).validate(isNull(), same(match), any(BuildActionDTO.class));
    }

    @Test
    void throwsWhenNoSettlementCanBePlacedAfterTheMaximumAttempts() throws GameBoard.NotAllFieldsFoundException {
        Match match = mock(Match.class);
        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        GameBoard gameBoard = mock(GameBoard.class);
        GamePlayers gamePlayers = mock(GamePlayers.class);
        PlayerRepresentation player = playerWithId(10);
        List<Structure> structures = new ArrayList<>();
        List<Field> fieldsWithDunes = List.of(
                RESOURCE_FIELDS.get(0),
                RESOURCE_FIELDS.get(1),
                new Field(AxialPosition.of(1, 0), 0, ResourceType.DUNES)
        );

        when(match.getGameBoard()).thenReturn(gameBoard);
        when(match.getPlayers()).thenReturn(gamePlayers);
        when(gamePlayers.getPlayers()).thenReturn(List.of(player));
        when(gameBoard.getStructures()).thenReturn(structures);
        when(gameBoard.getFieldsAt(anyList())).thenReturn(fieldsWithDunes);
        when(validator.getCorners()).thenReturn(Set.of(CORNER));
        when(validator.validate(isNull(), same(match), any(BuildActionDTO.class))).thenReturn(true);

        assertThrowsExactly(
                TooLittleSpaceException.class,
                () -> StructureFactory.randomlyBuildInitialStructures(match, validator)
        );
        assertTrue(structures.isEmpty());
    }

    @Test
    void retriesUnavailableAndRejectedPositionsUntilItCanBuild() throws GameBoard.NotAllFieldsFoundException {
        Match match = mock(Match.class);
        BuildingABuildingValidator validator = mock(BuildingABuildingValidator.class);
        GameBoard gameBoard = mock(GameBoard.class);
        GamePlayers gamePlayers = mock(GamePlayers.class);
        PlayerRepresentation player = playerWithId(10);
        List<Structure> structures = new ArrayList<>();
        AtomicInteger settlementValidations = new AtomicInteger();
        AtomicInteger streetValidations = new AtomicInteger();

        when(match.getGameBoard()).thenReturn(gameBoard);
        when(match.getPlayers()).thenReturn(gamePlayers);
        when(gamePlayers.getPlayers()).thenReturn(List.of(player));
        when(gameBoard.getStructures()).thenReturn(structures);
        when(gameBoard.getFieldsAt(anyList()))
                .thenThrow(new GameBoard.NotAllFieldsFoundException())
                .thenReturn(RESOURCE_FIELDS);
        when(validator.getCorners()).thenReturn(Set.of(CORNER));
        when(validator.validate(isNull(), same(match), any(BuildActionDTO.class))).thenAnswer(invocation -> {
            BuildActionDTO dto = invocation.getArgument(2);
            if (dto.getStructureType() == StructureType.SETTLEMENT) {
                return settlementValidations.incrementAndGet() > 1;
            }
            return streetValidations.incrementAndGet() > 3;
        });

        StructureFactory.randomlyBuildInitialStructures(match, validator);

        assertEquals(4, structures.size());
        assertInitialStructuresForPlayer(structures, 10);
        assertEquals(4, settlementValidations.get());
        assertEquals(5, streetValidations.get());
    }

    private static Stream<Arguments> recipes() {
        return Stream.of(
                Arguments.of(StructureType.SETTLEMENT, Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1,
                        ResourceType.SHEEP, 1,
                        ResourceType.WHEAT, 1
                )),
                Arguments.of(StructureType.STREET, Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1
                )),
                Arguments.of(StructureType.TOWN, Map.of(
                        ResourceType.BRICK, 4,
                        ResourceType.WHEAT, 2
                ))
        );
    }

    private static PlayerRepresentation playerWithId(int publicId) {
        PlayerRepresentation player = mock(PlayerRepresentation.class);
        when(player.getPublicId()).thenReturn(publicId);
        return player;
    }

    private static void assertInitialStructuresForPlayer(List<Structure> structures, int playerId) {
        List<Structure> playersStructures = structures.stream()
                .filter(structure -> structure.getOwnerId() == playerId)
                .toList();

        assertEquals(4, playersStructures.size());
        assertEquals(2, playersStructures.stream()
                .filter(structure -> structure.getType() == StructureType.SETTLEMENT)
                .count());
        assertEquals(2, playersStructures.stream()
                .filter(structure -> structure.getType() == StructureType.STREET)
                .count());
        assertTrue(playersStructures.stream()
                .filter(structure -> structure.getType() == StructureType.SETTLEMENT)
                .allMatch(structure -> structure.getPos().equals(CORNER)));
        assertTrue(playersStructures.stream()
                .filter(structure -> structure.getType() == StructureType.STREET)
                .allMatch(structure -> structure.getPos().size() == 2 && CORNER.containsAll(structure.getPos())));
    }
}
