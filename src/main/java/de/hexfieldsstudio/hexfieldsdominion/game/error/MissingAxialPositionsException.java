package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.InvalidDtoException;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

public class MissingAxialPositionsException extends InvalidDtoException {
    public MissingAxialPositionsException(StructureType type, int recievedAxialPositions) {
        super("Structure type '%s' with '%s' Axial Positions is invalid, expected: '%s'".formatted(type.name(), type.getPosAmount(), recievedAxialPositions));
    }
}
