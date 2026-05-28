package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MissingAxialPositionsException;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

import java.util.List;

public class BuildingABuildingValidator {
    public static boolean validate(Match match, BuildActionDTO buildActionDTO) throws Exception{
        StructureType type = buildActionDTO.getStructureType();
        if (buildActionDTO.getPos().size() != type.getPosAmount()) throw new MissingAxialPositionsException(type, buildActionDTO.getPos().size());
        List<Field> fields = match.getFields();


        return false;
    }
}
