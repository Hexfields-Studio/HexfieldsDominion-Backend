package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.util.Pair;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

@Getter
@Setter
public class BuildActionDTO extends PlayerActionDTO {

    private Pair<@NonNull Integer, @NonNull Integer>[] pos;
    private StructureType structureType;

}