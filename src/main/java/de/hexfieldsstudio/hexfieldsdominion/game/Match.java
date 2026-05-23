package de.hexfieldsstudio.hexfieldsdominion.game;

import java.util.*;

import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import lombok.Getter;
import lombok.Setter;


public class Match {

    private List<PlayerRepresentation> players;
    private final int boardRadius;
    @Getter
    private final List<Field> fields;
    private List<Structure> structures;
    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private Integer[] currentDiceResult = null;

    public Match(UUID uuid, int boardRadius){
        this.uuid = uuid;
        this.boardRadius = boardRadius;
        this.fields = generateFields();
    }

    private int calculateTotalResourceFields(){
        return 3 * boardRadius * (boardRadius - 1); //excluding center field
    }

    private List<Integer> generateNumberChips(){
        List<Integer> numberChips = new ArrayList<>(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        int totalFields = calculateTotalResourceFields();

        Random random = new Random();
        while (numberChips.size() < totalFields) {
            int chip = random.nextInt(11) + 2; // 2–12
            if (chip != 7) numberChips.add(chip);
        }

        Collections.shuffle(numberChips);
        return numberChips;
    }

    private List<ResourceType> generateAvailableResourceTypes(Map<ResourceType, Float> ratios){
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

    private List<Field> generateFields() {
        List<Field> fields = new ArrayList<>();

        List<ResourceType> availableResourceTypes = generateAvailableResourceTypes(Map.of(
                ResourceType.WOOD, 0.3f,
                ResourceType.BRICK, 0.2f,
                ResourceType.WHEAT, 0.3f,
                ResourceType.SHEEP, 0.2f
        ));
        List<Integer> numberChips = generateNumberChips();

        // https://www.redblobgames.com/grids/hexagons/#coordinates-axial
        for (int q = -boardRadius + 1; q <= boardRadius - 1; q++) {
            int r1 = Math.max(-boardRadius + 1, -q - boardRadius + 1);
            int r2 = Math.min(boardRadius - 1, -q + boardRadius - 1);
            for (int r = r1; r <= r2; r++) {
                if (q == 0 && r == 0) {
                    fields.add(new Field(new AxialPosition(q, r), 7, ResourceType.DUNES));
                    continue;
                }
                ResourceType type = availableResourceTypes.removeFirst();
                int numberChip = numberChips.removeFirst();
                fields.add(new Field(new AxialPosition(q, r), numberChip, type));
            }
        }

        return fields;
    }

}