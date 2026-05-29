package de.hexfieldsstudio.hexfieldsdominion.game.structure;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class Structure {
    private int publicPlayerId;
    private StructureType name;
    private List<AxialPosition> pos;
    private Map<ResourceType, Integer> recipe;
}
