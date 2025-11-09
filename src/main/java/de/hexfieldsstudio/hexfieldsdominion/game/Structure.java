package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import org.springframework.data.util.Pair;

import java.util.Map;

public class Structure {

    private String name;
    private Pair<Integer, Integer>[] pos;
    private Map<ResourceType, Integer> recipe;

}
