package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.NotFoundException;

import java.util.UUID;

public class MatchNotFoundException extends NotFoundException {
    public MatchNotFoundException(UUID matchUUID) {
        super("Match %s not found.".formatted(matchUUID.toString()));
    }
}
