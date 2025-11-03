package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import org.springframework.data.util.Pair;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

public class BuildActionDTO extends PlayerActionDTO{
    private Pair<Integer, Integer> index;
    private StructureType structureType;

    public void setIndex(Pair<Integer, Integer> index){
        this.index = index;
    }

    public void setStructureType(StructureType structureType){
        this.structureType = structureType;
    }

    public Pair getIndex(){
        return index;
    }

    public StructureType getStructureType(){
        return structureType;
    }
}