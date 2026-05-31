package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;

public class NotEnoughResourcesException extends BadRequestException {
    public NotEnoughResourcesException() {
        super("You don't have enough resources to do that.");
    }
}
