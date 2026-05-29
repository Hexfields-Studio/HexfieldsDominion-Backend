package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class TooLittleSpaceException extends BadRequestException {
    public TooLittleSpaceException() {
        super("Unable to initialize match. Cause: Too many players for given boardRadius.");
    }
}
