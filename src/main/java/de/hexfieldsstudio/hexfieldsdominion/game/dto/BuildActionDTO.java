package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BuildActionDTO extends PlayerActionDTO {

    private StructureType structureType;
    private List<AxialPosition> pos;
}