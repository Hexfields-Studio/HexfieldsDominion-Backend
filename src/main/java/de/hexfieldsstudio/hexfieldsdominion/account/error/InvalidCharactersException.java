package de.hexfieldsstudio.hexfieldsdominion.account.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class InvalidCharactersException extends BadRequestException {
    public InvalidCharactersException() {
        super("The credentials contain invalid characters.");
    }
}
