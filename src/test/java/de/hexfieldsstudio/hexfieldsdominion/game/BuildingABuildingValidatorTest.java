package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Field;
import de.hexfieldsstudio.hexfieldsdominion.game.board.GameBoard;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Structure;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MissingAxialPositionsException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.GamePlayers;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

class BuildingABuildingValidatorTest {

    private static final AxialPosition CENTER = AxialPosition.of(0, 0);
    private static final List<AxialPosition> VALID_EDGE = List.of(
            AxialPosition.of(0, 0),
            AxialPosition.of(1, 0)
    );
    private static final List<AxialPosition> VALID_CORNER = List.of(
            AxialPosition.of(0, 0),
            AxialPosition.of(1, -1),
            AxialPosition.of(1, 0)
    );

    private BuildingABuildingValidator validator;
    private Match match;
    private GameBoard gameBoard;
    private GamePlayers gamePlayers;
    private User user;

    @BeforeEach
    void setUp() {
        validator = new BuildingABuildingValidator(List.of(fieldAt(CENTER)));
        match = mock(Match.class);
        gameBoard = mock(GameBoard.class);
        gamePlayers = mock(GamePlayers.class);
        user = User.builder()
                .username("builder")
                .role(Role.GUEST)
                .build();

        when(match.getGameBoard()).thenReturn(gameBoard);
        when(match.getPlayers()).thenReturn(gamePlayers);
        when(gamePlayers.getPlayerForUser(nullable(User.class))).thenReturn(Optional.empty());
    }

    @Test
    void precomputesAllPointyTopCornersAndEdgesForAField() {
        Set<List<AxialPosition>> expectedCorners = Set.of(
                List.of(AxialPosition.of(0, -1), AxialPosition.of(0, 0), AxialPosition.of(1, -1)),
                List.of(AxialPosition.of(0, 0), AxialPosition.of(1, -1), AxialPosition.of(1, 0)),
                List.of(AxialPosition.of(0, 0), AxialPosition.of(0, 1), AxialPosition.of(1, 0)),
                List.of(AxialPosition.of(-1, 1), AxialPosition.of(0, 0), AxialPosition.of(0, 1)),
                List.of(AxialPosition.of(-1, 0), AxialPosition.of(-1, 1), AxialPosition.of(0, 0)),
                List.of(AxialPosition.of(-1, 0), AxialPosition.of(0, -1), AxialPosition.of(0, 0))
        );
        Set<List<AxialPosition>> expectedEdges = Set.of(
                List.of(AxialPosition.of(0, 0), AxialPosition.of(1, -1)),
                List.of(AxialPosition.of(0, 0), AxialPosition.of(1, 0)),
                List.of(AxialPosition.of(0, 0), AxialPosition.of(0, 1)),
                List.of(AxialPosition.of(-1, 1), AxialPosition.of(0, 0)),
                List.of(AxialPosition.of(-1, 0), AxialPosition.of(0, 0)),
                List.of(AxialPosition.of(0, -1), AxialPosition.of(0, 0))
        );

        assertEquals(expectedCorners, validator.getCorners());
        assertEquals(expectedEdges, validator.getEdges());
    }

    @Test
    void deduplicatesCornersAndEdgesSharedByAdjacentFields() {
        BuildingABuildingValidator adjacentFieldsValidator = new BuildingABuildingValidator(List.of(
                fieldAt(AxialPosition.of(0, 0)),
                fieldAt(AxialPosition.of(1, 0))
        ));

        assertEquals(10, adjacentFieldsValidator.getCorners().size());
        assertEquals(11, adjacentFieldsValidator.getEdges().size());
    }

    @ParameterizedTest
    @EnumSource(StructureType.class)
    void rejectsAnIncorrectNumberOfAxialPositions(StructureType structureType) {
        List<AxialPosition> tooFewPositions = IntStream.range(0, structureType.getPosAmount() - 1)
                .mapToObj(i -> AxialPosition.of(i, 0))
                .toList();
        BuildActionDTO dto = new BuildActionDTO(structureType, tooFewPositions);

        assertThrowsExactly(MissingAxialPositionsException.class, () -> validator.validate(user, match, dto));
    }

    @Test
    void sortsAValidPositionBeforeValidatingAndStoringItInTheDto() {
        BuildActionDTO dto = new BuildActionDTO(
                StructureType.STREET,
                List.of(AxialPosition.of(1, 0), AxialPosition.of(0, 0))
        );

        assertTrue(validator.validate(null, match, dto));
        assertEquals(VALID_EDGE, dto.getPos());
    }

    @ParameterizedTest
    @MethodSource("invalidBoardPositions")
    void rejectsPositionsThatAreNotPartOfTheBoard(StructureType type, List<AxialPosition> positions) {
        BuildActionDTO dto = new BuildActionDTO(type, positions);

        assertFalse(validator.validate(null, match, dto));
    }

    @Test
    void rejectsAnOccupiedBuildingSpot() {
        Structure existingStreet = new Structure(StructureType.STREET, VALID_EDGE, 1, Map.of());
        when(gameBoard.getStructureAt(VALID_EDGE)).thenReturn(existingStreet);
        BuildActionDTO dto = new BuildActionDTO(StructureType.STREET, VALID_EDGE);

        assertFalse(validator.validate(null, match, dto));
    }

    @ParameterizedTest
    @MethodSource("insufficientResourceInventories")
    void rejectsAPlayerWhoCannotPayTheCompleteRecipe(Map<ResourceType, Integer> resources) {
        PlayerRepresentation player = playerWithResources(7, resources);
        when(gamePlayers.getPlayerForUser(user)).thenReturn(Optional.of(player));
        BuildActionDTO dto = new BuildActionDTO(StructureType.SETTLEMENT, VALID_CORNER);

        assertFalse(validator.validate(user, match, dto));
        verifyNoInteractions(gameBoard);
    }

    @Test
    void rejectsATownUpgradeWhenTheUserIsNotAMatchPlayer() {
        BuildActionDTO dto = new BuildActionDTO(StructureType.TOWN, VALID_CORNER);

        assertFalse(validator.validate(user, match, dto));
        verifyNoInteractions(gameBoard);
    }

    @ParameterizedTest
    @MethodSource("connectedBuildingScenarios")
    void requiresAConnectionToOneOfThePlayersStructures(
            StructureType requestedType,
            List<AxialPosition> requestedPosition,
            StructureType neighbourType,
            List<AxialPosition> neighbourPosition,
            int neighbourOwnerId,
            boolean expected
    ) {
        int playerId = 7;
        PlayerRepresentation player = playerWithResources(playerId, allResources(10));
        when(gamePlayers.getPlayerForUser(user)).thenReturn(Optional.of(player));

        Structure neighbour = new Structure(neighbourType, neighbourPosition, neighbourOwnerId, Map.of());
        when(gameBoard.getStructureAt(anyList())).thenAnswer(invocation ->
                neighbourPosition.equals(invocation.<List<AxialPosition>>getArgument(0)) ? neighbour : null
        );

        BuildActionDTO dto = new BuildActionDTO(requestedType, requestedPosition);

        assertEquals(expected, validator.validate(user, match, dto));
    }

    @Test
    void allowsAPlayerToUpgradeTheirOwnSettlementToATown() {
        int playerId = 7;
        PlayerRepresentation player = playerWithResources(playerId, allResources(10));
        when(gamePlayers.getPlayerForUser(user)).thenReturn(Optional.of(player));
        when(gameBoard.getStructureAt(VALID_CORNER)).thenReturn(
                new Structure(StructureType.SETTLEMENT, VALID_CORNER, playerId, Map.of())
        );
        BuildActionDTO dto = new BuildActionDTO(StructureType.TOWN, VALID_CORNER);

        assertTrue(validator.validate(user, match, dto));
    }

    @ParameterizedTest
    @MethodSource("invalidTownUpgradeTargets")
    void rejectsAnInvalidTownUpgradeTarget(Structure target) {
        PlayerRepresentation player = playerWithResources(7, allResources(10));
        when(gamePlayers.getPlayerForUser(user)).thenReturn(Optional.of(player));
        when(gameBoard.getStructureAt(VALID_CORNER)).thenReturn(target);
        BuildActionDTO dto = new BuildActionDTO(StructureType.TOWN, VALID_CORNER);

        assertFalse(validator.validate(user, match, dto));
    }

    private static Stream<Arguments> invalidBoardPositions() {
        return Stream.of(
                Arguments.of(
                        StructureType.STREET,
                        List.of(AxialPosition.of(0, 0), AxialPosition.of(2, 0))
                ),
                Arguments.of(
                        StructureType.SETTLEMENT,
                        List.of(AxialPosition.of(0, 0), AxialPosition.of(1, 0), AxialPosition.of(2, 0))
                )
        );
    }

    private static Stream<Arguments> connectedBuildingScenarios() {
        return Stream.of(
                Arguments.of(StructureType.SETTLEMENT, VALID_CORNER, StructureType.STREET, VALID_EDGE, 7, true),
                Arguments.of(StructureType.SETTLEMENT, VALID_CORNER, StructureType.STREET, VALID_EDGE, 8, false),
                Arguments.of(StructureType.STREET, VALID_EDGE, StructureType.SETTLEMENT, VALID_CORNER, 7, true),
                Arguments.of(StructureType.STREET, VALID_EDGE, StructureType.SETTLEMENT, VALID_CORNER, 8, false)
        );
    }

    private static Stream<Map<ResourceType, Integer>> insufficientResourceInventories() {
        return Stream.of(
                Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1,
                        ResourceType.WHEAT, 1
                ),
                Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1,
                        ResourceType.WHEAT, 1,
                        ResourceType.SHEEP, 0
                )
        );
    }

    private static Stream<Structure> invalidTownUpgradeTargets() {
        return Stream.of(
                null,
                new Structure(StructureType.SETTLEMENT, VALID_CORNER, 8, Map.of()),
                new Structure(StructureType.STREET, VALID_CORNER, 7, Map.of()),
                new Structure(StructureType.TOWN, VALID_CORNER, 7, Map.of())
        );
    }

    private PlayerRepresentation playerWithResources(int publicId, Map<ResourceType, Integer> resources) {
        PlayerRepresentation player = mock(PlayerRepresentation.class);
        when(player.getPublicId()).thenReturn(publicId);
        when(player.getResources()).thenReturn(resources);
        return player;
    }

    private static Map<ResourceType, Integer> allResources(int amount) {
        return Map.of(
                ResourceType.WOOD, amount,
                ResourceType.BRICK, amount,
                ResourceType.WHEAT, amount,
                ResourceType.SHEEP, amount
        );
    }

    private static Field fieldAt(AxialPosition position) {
        return new Field(position, 5, ResourceType.WOOD);
    }
}
