package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class Structure {

    @Getter
    private final StructureType type;
    @Getter
    private final List<AxialPosition> pos;
    @Getter
    private final int ownerId;
    private Map<ResourceType, Integer> recipe;

}
