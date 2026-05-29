package de.hexfieldsstudio.hexfieldsdominion.game.types;

import lombok.Getter;

@Getter
public enum StructureType {
    TOWN(3), HARBOUR(3), STREET(2);
    private final int posAmount;

    StructureType(int posAmount){
        this.posAmount = posAmount;
    }
}