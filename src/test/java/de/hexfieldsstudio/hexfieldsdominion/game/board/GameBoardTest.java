package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class GameBoardTest {

    private static final int BOARD_RADIUS = 3;
    private static final Map<ResourceType, Float> RATIOS = Map.of(
            ResourceType.WOOD, 0.3f,
            ResourceType.BRICK, 0.2f,
            ResourceType.WHEAT, 0.3f,
            ResourceType.SHEEP, 0.2f
    );

    private GameBoard gameBoard;

    @BeforeAll
    @SuppressWarnings("resource") // AutoClosable mockStatic
    static void setup() {
        List<Field> fields = List.of(
                new Field(AxialPosition.of(1, 1), 2, ResourceType.BRICK),
                new Field(AxialPosition.of(-1, -1), 11, ResourceType.WHEAT),
                new Field(AxialPosition.of(2, 2), 5, ResourceType.SHEEP),
                new Field(AxialPosition.of(-2, -2), 11, ResourceType.BRICK)
        );

        MockedStatic<FieldFactory> mockedFieldFactory = mockStatic(FieldFactory.class);
        mockedFieldFactory.when(() -> FieldFactory.generateFields(BOARD_RADIUS, RATIOS)).thenReturn(fields);
    }

    @BeforeEach
    void setupEach() {
        gameBoard = new GameBoard(BOARD_RADIUS);
    }

    @ParameterizedTest
    @ArgumentsSource(StructureTypesProvider.class)
    void testAddStructure(StructureType structureType) {
        PlayerRepresentation player = this.createPlayerRepresentation();

        BuildActionDTO dto = new BuildActionDTO(
                structureType,
                List.of(AxialPosition.of(1, 1), AxialPosition.of(-1, -1))
        );

        gameBoard.addStructure(player, dto);

        assertEquals(1, gameBoard.getStructures().size());
        Structure addedStructure = gameBoard.getStructures().getFirst();
        assertEquals(player.getPublicId(), addedStructure.getOwnerId());
        assertEquals(dto.getStructureType(), addedStructure.getType());
        assertEquals(dto.getPos(), addedStructure.getPos());
    }

    @Test
    void testUpgradeSettlementToTown() {
        PlayerRepresentation player = this.createPlayerRepresentation();

        Structure structure = this.createBasicStructure(StructureType.SETTLEMENT);
        gameBoard.getStructures().add(structure);

        BuildActionDTO dto = new BuildActionDTO(
                StructureType.TOWN,
                List.of(AxialPosition.of(1, 1))
        );

        gameBoard.upgradeSettlementToTown(player, dto);

        assertEquals(1, gameBoard.getStructures().size());
        Structure storedStructure = gameBoard.getStructures().getFirst();
        assertEquals(StructureType.TOWN, storedStructure.getType());
        assertEquals(structure.getOwnerId(), storedStructure.getOwnerId());
        assertEquals(structure.getPos(), storedStructure.getPos());
    }

    @Test
    void testUpgradeSettlementToTownNoStructureAtPos() {
        PlayerRepresentation player = this.createPlayerRepresentation();

        Structure structure = this.createBasicStructure(StructureType.SETTLEMENT);
        gameBoard.getStructures().add(structure);

        BuildActionDTO dto = new BuildActionDTO(
                StructureType.TOWN,
                List.of(AxialPosition.of(-1, -1))
        );

        gameBoard.upgradeSettlementToTown(player, dto);

        assertEquals(1, gameBoard.getStructures().size());
        assertEquals(structure, gameBoard.getStructures().getFirst());
    }

    @ParameterizedTest
    @ArgumentsSource(FieldsAtPositionsProvider.class)
    void testGetFields(List<AxialPosition> positions) throws GameBoard.NotAllFieldsFoundException {
        List<Field> fields = gameBoard.getFieldsAt(positions);

        assertEquals(positions.size(), fields.size());
        for (AxialPosition pos : positions) {
            assertTrue(fields.stream().anyMatch(field -> field.pos().equals(pos)));
        }
    }

    @Test
    void testGetFieldsNotAllExist() {
        AxialPosition pos = AxialPosition.of(5, 5);

        assertThrowsExactly(GameBoard.NotAllFieldsFoundException.class, () -> gameBoard.getFieldsAt(List.of(pos)));
    }

    @ParameterizedTest
    @ArgumentsSource(StructureTypesProvider.class)
    void testGetStructureAtExists(StructureType structureType) {
        Structure structure = this.createBasicStructure(structureType);
        gameBoard.getStructures().add(structure);

        Structure foundStructure = gameBoard.getStructureAt(List.of(AxialPosition.of(1, 1)));

        assertNotNull(foundStructure);
        assertEquals(structure, foundStructure);
    }

    @Test
    void testGetStructureAtNotExists() {
        Structure foundStructure = gameBoard.getStructureAt(List.of(AxialPosition.of(1, 1)));

        assertNull(foundStructure);
    }

    @Test
    void testGetFieldsByNumberChip() {
        List<Field> fields = List.of(
                new Field(AxialPosition.of(1, 1), 2, ResourceType.BRICK),
                new Field(AxialPosition.of(-1, -1), 11, ResourceType.WHEAT),
                new Field(AxialPosition.of(2, 2), 5, ResourceType.SHEEP),
                new Field(AxialPosition.of(-2, -2), 11, ResourceType.BRICK)
        );

        // 1 exists
        List<Field> fieldsNumberChip2 = gameBoard.getFieldsByNumberChip(2);
        assertEquals(1, fieldsNumberChip2.size());
        assertEquals(fields.getFirst(), fieldsNumberChip2.getFirst());

        // 2 exist
        List<Field> fieldsNumberChip11 = gameBoard.getFieldsByNumberChip(11);
        assertEquals(2, fieldsNumberChip11.size());
        assertEquals(fields.get(1), fieldsNumberChip11.getFirst());
        assertEquals(fields.get(3), fieldsNumberChip11.getLast());

        // doesn't exist
        List<Field> fieldsNumberChip9 = gameBoard.getFieldsByNumberChip(9);
        assertEquals(0, fieldsNumberChip9.size());
    }

    static class StructureTypesProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Arrays.stream(StructureType.values())
                    .map(Arguments::of);
        }
    }

    static class FieldsAtPositionsProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(List.of()),
                    Arguments.of(List.of(
                            AxialPosition.of(1, 1)
                    )),
                    Arguments.of(List.of(
                            AxialPosition.of(1, 1),
                            AxialPosition.of(-1, -1)
                    ))
            );
        }
    }

    private PlayerRepresentation createPlayerRepresentation() {
        User user = User.builder()
                .username("test")
                .role(Role.GUEST)
                .build();

        Player playerBase = new Player(user, 0);
        return new PlayerRepresentation(playerBase);
    }

    private Structure createBasicStructure(StructureType structureType) {
        return new Structure(
                structureType,
                List.of(AxialPosition.of(1, 1)),
                0,
                Map.of()
        );
    }

}