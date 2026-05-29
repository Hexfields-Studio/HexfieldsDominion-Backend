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
    private final Set<List<AxialPosition>> edges;
    private static final List<AxialPosition> edgeOffsetToAdjacentField = List.of(
            AxialPosition.of(1, -1),
            AxialPosition.of(1, 0),
            AxialPosition.of(0, 1),
            AxialPosition.of(-1, 1),
            AxialPosition.of(-1, 0),
            AxialPosition.of(0, -1)

    );

    public BuildingABuildingValidator(List<Field> fields){
        corners = computeUniqueCorners(fields);
        edges = computeUniqueEdges(fields);
    }

    public boolean validate(Match match, BuildActionDTO buildActionDTO) throws MissingAxialPositionsException{
        StructureType type = buildActionDTO.getStructureType();
        if (buildActionDTO.getPos().size() != type.getPosAmount()) throw new MissingAxialPositionsException(type, buildActionDTO.getPos().size());
        List<AxialPosition> sortedPos = getSortedPosition(buildActionDTO.getPos());

        boolean isValid = isPositionValid(sortedPos);
        switch (type){
            case TOWN -> isValid = corners.contains(sortedPos);
            case STREET -> isValid = edges.contains(sortedPos);
        }
        System.out.printf("Type: %s; pos: %s; isValid: %s%n", type, sortedPos, isValid);
        if (!isValid) return false;

        buildActionDTO.setPos(sortedPos);
        Map<AxialPosition, Field> map = Field.getFieldsMap(match.getFields());

        return true;
    }

    private boolean isPositionValid(List<AxialPosition> pos){ //Remember to sort pos first!
        return corners.contains(pos);
    }

    private Set<List<AxialPosition>> computeUniqueCorners(List<Field> fields){
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

    private Set<List<AxialPosition>> computeUniqueEdges(List<Field> fields){
        Set<List<AxialPosition>> edgeMap = new HashSet<>();

        for (Field field : fields){
            AxialPosition pos = field.pos();

            for (int i = 0; i < 6; i++){
                AxialPosition offset = edgeOffsetToAdjacentField.get(i);
                List<AxialPosition> edgePos = new ArrayList<>(List.of(
                        pos,
                        AxialPosition.of(pos.q() + offset.q(), pos.r() + offset.r())
                ));
                edgeMap.add(getSortedPosition(edgePos));
            }
        }
        return edgeMap;
    }

    private List<AxialPosition> getSortedPosition(List<AxialPosition> pos){
        List<AxialPosition> sorted = new ArrayList<>(pos);
        sorted.sort(Comparator.comparingInt(AxialPosition::q)
                .thenComparingInt(AxialPosition::r));
        return List.copyOf(sorted);
    }
}