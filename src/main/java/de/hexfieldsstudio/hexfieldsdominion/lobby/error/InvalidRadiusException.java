package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

public class InvalidRadiusException extends RuntimeException {
    public InvalidRadiusException(int boardRadius) {
        super("error: Radius invalid for %s. It must be between 3 and 6.".formatted(boardRadius));
    }
}
