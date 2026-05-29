package de.hexfieldsstudio.hexfieldsdominion.game.field;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;

import java.security.SecureRandom;
import java.util.*;

public class FieldFactory {

    private static int boardRadius;

    public static List<Field> generateFields(int boardRadius, Map<ResourceType, Float> ratios) {
        FieldFactory.boardRadius = boardRadius;

        List<Field> fields = new ArrayList<>();

        List<ResourceType> availableResourceTypes = generateAvailableResourceTypes(ratios);
        List<Integer> numberChips = generateNumberChips();

        // https://www.redblobgames.com/grids/hexagons/#coordinates-axial (pointy)
        for (int q = -boardRadius + 1; q <= boardRadius - 1; q++) {
            int r1 = Math.max(-boardRadius + 1, -q - boardRadius + 1);
            int r2 = Math.min(boardRadius - 1, -q + boardRadius - 1);
            for (int r = r1; r <= r2; r++) {
                if (q == 0 && r == 0) {
                    fields.add(new Field(new AxialPosition(q, r), 0, ResourceType.DUNES));
                    continue;
                }
                ResourceType type = availableResourceTypes.removeFirst();
                int numberChip = numberChips.removeFirst();
                fields.add(new Field(new AxialPosition(q, r), numberChip, type));
            }
        }

        return fields;
    }

    private static int calculateTotalResourceFields(){
        return 3 * boardRadius * (boardRadius - 1); //excluding center field
    }

    private static List<Integer> generateNumberChips(){
        List<Integer> numberChips = new ArrayList<>(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        int totalFields = calculateTotalResourceFields();

        SecureRandom random = new SecureRandom();
        while (numberChips.size() < totalFields) {
            int chip = random.nextInt(11) + 2; // 2–12
            if (chip != 7) numberChips.add(chip);
        }

        Collections.shuffle(numberChips);
        return numberChips;
    }

    private static List<ResourceType> generateAvailableResourceTypes(Map<ResourceType, Float> ratios){
        int totalFields = calculateTotalResourceFields();

        List<ResourceType> types = new ArrayList<>(ratios.keySet());
        Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);

        int used = 0;

        // initial floor allocation
        for (var e : ratios.entrySet()) {
            int count = (int) Math.floor(totalFields * e.getValue());
            counts.put(e.getKey(), count);
            used += count;
        }

        // distribute resources on remaining fields
        int i = 0;
        while (used < totalFields) {
            ResourceType t = types.get(i % types.size());
            counts.put(t, counts.get(t) + 1);
            used++;
            i++;
        }

        List<ResourceType> result = new ArrayList<>(totalFields);
        for (var e : counts.entrySet()) {
            for (int j = 0; j < e.getValue(); j++) {
                result.add(e.getKey());
            }
        }

        Collections.shuffle(result);
        return result;
    }
}
