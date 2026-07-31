package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FieldFactoryTest {

    private static final Map<ResourceType, Float> STANDARD_RATIOS = standardRatios();

    @Test
    void generatesEveryPointyTopAxialPositionInsideTheRadius() {
        int boardRadius = 3;

        List<Field> fields = FieldFactory.generateFields(boardRadius, STANDARD_RATIOS);
        Set<AxialPosition> uniquePositions = fields.stream()
                .map(Field::pos)
                .collect(Collectors.toSet());

        assertEquals(19, fields.size());
        assertEquals(fields.size(), uniquePositions.size());
        assertTrue(uniquePositions.contains(AxialPosition.of(0, 0)));
        assertTrue(uniquePositions.stream().allMatch(position -> axialDistanceFromCenter(position) < boardRadius));
    }

    @Test
    void placesTheDunesWithoutANumberChipAtTheCenter() {
        List<Field> fields = FieldFactory.generateFields(3, STANDARD_RATIOS);

        Field center = fields.stream()
                .filter(field -> field.pos().equals(AxialPosition.of(0, 0)))
                .findFirst()
                .orElseThrow();

        assertEquals(ResourceType.DUNES, center.resource());
        assertEquals(0, center.numberChip());
        assertEquals(1, fields.stream().filter(field -> field.resource() == ResourceType.DUNES).count());
    }

    @Test
    void allocatesResourceFieldsAccordingToTheGivenRatios() {
        List<Field> fields = FieldFactory.generateFields(3, STANDARD_RATIOS);
        Map<ResourceType, Long> resourceCounts = fields.stream()
                .filter(field -> !field.pos().equals(AxialPosition.of(0, 0)))
                .collect(Collectors.groupingBy(Field::resource, Collectors.counting()));

        assertEquals(6L, resourceCounts.get(ResourceType.WOOD));
        assertEquals(4L, resourceCounts.get(ResourceType.BRICK));
        assertEquals(5L, resourceCounts.get(ResourceType.WHEAT));
        assertEquals(3L, resourceCounts.get(ResourceType.SHEEP));
        assertFalse(resourceCounts.containsKey(ResourceType.DUNES));
    }

    @Test
    void usesTheClassicCatanNumberChipDistributionForRadiusThree() {
        List<Integer> expectedNumberChips = List.of(
                2, 3, 3, 4, 4, 5, 5, 6, 6,
                8, 8, 9, 9, 10, 10, 11, 11, 12
        );
        List<Integer> actualNumberChips = FieldFactory.generateFields(3, STANDARD_RATIOS).stream()
                .filter(field -> !field.pos().equals(AxialPosition.of(0, 0)))
                .map(Field::numberChip)
                .sorted()
                .toList();

        assertEquals(expectedNumberChips, actualNumberChips);
    }

    @Test
    void generatesOnlyValidAdditionalNumberChipsForLargerBoards() {
        List<Field> fields = FieldFactory.generateFields(4, STANDARD_RATIOS);

        assertEquals(37, fields.size());
        assertTrue(fields.stream()
                .filter(field -> !field.pos().equals(AxialPosition.of(0, 0)))
                .map(Field::numberChip)
                .allMatch(numberChip -> numberChip >= 2 && numberChip <= 12 && numberChip != 7));
    }

    @Test
    void radiusOneContainsOnlyTheCenterDunesField() {
        List<Field> fields = FieldFactory.generateFields(1, STANDARD_RATIOS);

        assertEquals(List.of(new Field(AxialPosition.of(0, 0), 0, ResourceType.DUNES)), fields);
    }

    private static int axialDistanceFromCenter(AxialPosition position) {
        return Math.max(
                Math.max(Math.abs(position.q()), Math.abs(position.r())),
                Math.abs(position.q() + position.r())
        );
    }

    private static Map<ResourceType, Float> standardRatios() {
        Map<ResourceType, Float> ratios = new LinkedHashMap<>();
        ratios.put(ResourceType.WOOD, 0.3f);
        ratios.put(ResourceType.BRICK, 0.2f);
        ratios.put(ResourceType.WHEAT, 0.3f);
        ratios.put(ResourceType.SHEEP, 0.2f);
        return ratios;
    }
}
