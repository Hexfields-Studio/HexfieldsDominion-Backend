package de.hexfieldsstudio.hexfieldsdominion.game;

import java.util.*;

import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.Pair;


public class Match {

    private List<PlayerRepresentation> players;
    @Getter
    private List<Field> fields;
    private List<Structure> structures;
    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private Integer[] currentDiceResult = null;
    private int boardRadius;

    public Match(UUID uuid, int boardRadius){
        this.uuid = uuid;
        this.boardRadius = boardRadius;
        this.fields = generateFields();
    }

    private int calculateTotalFields(){
        return 3 * boardRadius * (boardRadius - 1); //excluding center field
    }

    private List<Integer> generateNumberChips(){
        List<Integer> numberChips = new ArrayList<>(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        int totalFields = calculateTotalFields();

        for (int i = numberChips.size(); i < totalFields; i++){
            int randomChip = (int) (Math.random() * 11) + 2;
            if(randomChip == 7) randomChip = numberChips.get((int) (Math.random() * numberChips.size()));
            numberChips.add(randomChip);
        }

        Collections.shuffle(numberChips);
        return numberChips;
    }

    private List<ResourceType> generateAvailableResourceTypes(Map<ResourceType, Float> ratios){
        Map<ResourceType, Integer> availableResourceCount = new EnumMap<>(ResourceType.class);
        int totalFields = calculateTotalFields();

        int fieldsWithResources = 0;
        for (var entry : ratios.entrySet()) {
            int count = (int) Math.floor(totalFields * entry.getValue());
            availableResourceCount.put(entry.getKey(), count);
            fieldsWithResources += count;
        }

        int remaining = totalFields - fieldsWithResources;
        List<ResourceType> types = new ArrayList<>(ratios.keySet());

        int i = 0;
        while (remaining > 0) {
            ResourceType type = types.get(i % types.size());
            availableResourceCount.put(type, availableResourceCount.get(type) + 1);
            remaining--;
            i++;
        }

        List<ResourceType> availableResourceTypes = new ArrayList<>();

        for (var entry : availableResourceCount.entrySet()) {
            for (int count = 0; count < entry.getValue(); count++) {
                availableResourceTypes.add(entry.getKey());
            }
        }

        Collections.shuffle(availableResourceTypes);
        return availableResourceTypes;
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
                    fields.add(new Field(Pair.of(q, r), 7, new Resource(ResourceType.DUNES.toString())));
                    continue;
                }
                ResourceType type = availableResourceTypes.removeFirst();
                int numberChip = numberChips.removeFirst();
                fields.add(new Field(Pair.of(q, r), numberChip, new Resource(type.toString())));
            }
        }

        return fields;
    }

}