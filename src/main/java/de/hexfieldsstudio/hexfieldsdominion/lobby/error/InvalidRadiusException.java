package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class InvalidRadiusException extends BadRequestException {
    public InvalidRadiusException(int boardRadius) {
        super("error: Radius invalid for %s. It must be between 3 and 6.".formatted(boardRadius));
    }
}
