package de.hexfieldsstudio.hexfieldsdominion.account.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class UserAlreadyExistsException extends BadRequestException {
    public UserAlreadyExistsException() {
        super("There is already an user with this name.");
    }
}
