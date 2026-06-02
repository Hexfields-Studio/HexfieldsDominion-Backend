package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameBoard {

    private static final Map<ResourceType, Float> RATIOS = Map.of(
            ResourceType.WOOD, 0.3f,
            ResourceType.BRICK, 0.2f,
            ResourceType.WHEAT, 0.3f,
            ResourceType.SHEEP, 0.2f
    );

    @Getter
    private final List<Field> fields;
    @Getter
    private final List<Structure> structures = new ArrayList<>();

    public GameBoard(int boardRadius) {
        this.fields = FieldFactory.generateFields(boardRadius, RATIOS);
    }

    public void addStructure(PlayerRepresentation player, BuildActionDTO buildActionDTO) {
        this.structures.add(StructureFactory.buildStructureFromDTO(player, buildActionDTO));
    }

    public void upgradeSettlementToTown(PlayerRepresentation player, BuildActionDTO buildActionDTO){
        Structure settlement = getStructureAt(buildActionDTO.getPos());
        if(settlement == null) return;
        this.structures.remove(settlement);
        this.structures.add(StructureFactory.buildStructureFromDTO(player, buildActionDTO));
    }

    public List<Field> getFieldsAt(List<AxialPosition> positions) throws NotAllFieldsFoundException {
        List<Field> fieldsFound = new ArrayList<>();
        for (Field field : fields) {
            if (!positions.contains(field.pos())) {
                continue;
            }
            fieldsFound.add(field);
        }

        if (fieldsFound.size() != positions.size()) {
            throw new NotAllFieldsFoundException();
        }
        return fieldsFound;
    }

    public Structure getStructureAt(List<AxialPosition> pos) {
        for (Structure structure: structures){
            if (structure.getPos().equals(pos)) return structure;
        }
        return null;
    }

    public List<Field> getFieldsByNumberChip(int numberChip) {
        return fields.stream()
                .filter(field -> field.numberChip() == numberChip)
                .toList();
    }

    public static class NotAllFieldsFoundException extends Exception {}

}
