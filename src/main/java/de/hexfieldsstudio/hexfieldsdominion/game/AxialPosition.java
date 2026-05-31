package de.hexfieldsstudio.hexfieldsdominion.game;

public record AxialPosition(int q, int r) {
    public static AxialPosition of(int q, int r){
        return new AxialPosition(q, r);
    }
}
