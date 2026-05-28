package de.hexfieldsstudio.hexfieldsdominion.game.types;

public enum StructureType {
    TOWN(3), HARBOUR(3), STREET(2);

    private final int posAmount;

    StructureType(int posAmount){
        this.posAmount = posAmount;
    }

    public int getPosAmount(){
        return posAmount;
    }
}