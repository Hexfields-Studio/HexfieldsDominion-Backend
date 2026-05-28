package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

import java.util.List;
import java.util.Map;

public class Structure {

    private StructureType name;
    private List<AxialPosition> pos;
    private Map<ResourceType, Integer> recipe;

}
