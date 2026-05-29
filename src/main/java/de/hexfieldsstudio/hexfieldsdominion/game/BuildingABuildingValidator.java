package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MissingAxialPositionsException;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

import java.util.*;

public class BuildingABuildingValidator {

    private final Set<List<AxialPosition>> corners;
    private static final List<List<AxialPosition>> cornerOffsetToAdjacentFields = new ArrayList<>(List.of(
            List.of(AxialPosition.of(0, -1), AxialPosition.of(1, -1)),
            List.of(AxialPosition.of(1, -1), AxialPosition.of(1, 0)),
            List.of(AxialPosition.of(1, 0), AxialPosition.of(0, 1)),
            List.of(AxialPosition.of(0, 1), AxialPosition.of(-1, 1)),
            List.of(AxialPosition.of(-1, 1), AxialPosition.of(-1, 0)),
            List.of(AxialPosition.of(-1, 0), AxialPosition.of(0, -1))
    ));

    public BuildingABuildingValidator(List<Field> fields){
        //Precompute Corners and Edges
        corners = preComputeUniqueCornerPositions(fields);
    }

    public boolean validate(Match match, BuildActionDTO buildActionDTO) throws MissingAxialPositionsException{
        StructureType type = buildActionDTO.getStructureType();
        if (buildActionDTO.getPos().size() != type.getPosAmount()) throw new MissingAxialPositionsException(type, buildActionDTO.getPos().size());
        List<AxialPosition> sortedPos = getSortedPosition(buildActionDTO.getPos());
        if (!isPositionValid(sortedPos)) return false;
        buildActionDTO.setPos(sortedPos);

        return true;
    }

    private boolean isPositionValid(List<AxialPosition> pos){ //Remember to sort pos first!
        return corners.contains(pos);
    }

    private Set<List<AxialPosition>> preComputeUniqueCornerPositions(List<Field> fields){
        Set<List<AxialPosition>> cornerMap = new HashSet<>();
        for (Field field : fields){
            AxialPosition pos = field.pos();

            for (int i = 0; i < 6; i++){
                AxialPosition offset0 = cornerOffsetToAdjacentFields.get(i).get(0);
                AxialPosition offset1 = cornerOffsetToAdjacentFields.get(i).get(1);

                List<AxialPosition> cornerPos = new ArrayList<>(List.of(
                        pos,
                        AxialPosition.of(pos.q() + offset0.q(), pos.r() + offset0.r()),
                        AxialPosition.of(pos.q() + offset1.q(), pos.r() + offset1.r())
                ));
                List<AxialPosition> sorted = getSortedPosition(cornerPos);
                cornerMap.add(sorted);
            }
        }
        return cornerMap;
    }

    private List<AxialPosition> getSortedPosition(List<AxialPosition> pos){
        List<AxialPosition> sorted = new ArrayList<>(pos);
        sorted.sort(Comparator.comparingInt(AxialPosition::q)
                .thenComparingInt(AxialPosition::r));
        return List.copyOf(sorted);
    }
}