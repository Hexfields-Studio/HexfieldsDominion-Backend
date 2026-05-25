package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.NotFoundException;

import java.util.UUID;

public class MatchNotFoundException extends NotFoundException {
    public MatchNotFoundException(UUID matchUUID) {
        super("Es wurde kein Match %s gefunden.".formatted(matchUUID.toString()));
    }
}
