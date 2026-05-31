package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.ForbiddenException;

public class NotPlayersTurnException extends ForbiddenException {
    public NotPlayersTurnException() {
        super("It's not your turn.");
    }
}
