package de.hexfieldsstudio.hexfieldsdominion.account.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class UserAlreadyExistsException extends BadRequestException {
    public UserAlreadyExistsException() {
        super("Es existiert bereits ein Nutzer mit diesem Namen.");
    }
}
