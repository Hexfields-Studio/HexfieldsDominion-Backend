package de.hexfieldsstudio.hexfieldsdominion.account.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class InvalidCredentialsException extends BadRequestException {
    public InvalidCredentialsException() {
        super("Ungültige Zugangsdaten.");
    }
}
