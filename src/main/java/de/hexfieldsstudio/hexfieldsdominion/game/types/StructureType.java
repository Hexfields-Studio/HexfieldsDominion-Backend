package de.hexfieldsstudio.hexfieldsdominion.game.types;

import lombok.Getter;

@Getter
public enum StructureType {
    SETTLEMENT(3), TOWN(3), STREET(2);
    private final int posAmount;

    StructureType(int posAmount){
        this.posAmount = posAmount;
    }
}