package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.ForbiddenException;

public class NotPlayersTurnException extends ForbiddenException {
    public NotPlayersTurnException() {
        super("Du bist gerade nicht am Zug.");
    }
}
