package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class InvalidBuildRequestException extends BadRequestException {
    public InvalidBuildRequestException() {
        super("Placing a building here is invalid.");
    }
}
