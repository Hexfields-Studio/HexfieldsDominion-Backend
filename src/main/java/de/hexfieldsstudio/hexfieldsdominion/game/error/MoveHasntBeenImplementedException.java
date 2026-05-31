package de.hexfieldsstudio.hexfieldsdominion.game.error;

import de.hexfieldsstudio.hexfieldsdominion.error.BadRequestException;
import de.hexfieldsstudio.hexfieldsdominion.game.types.PlayerActionType;

public class MoveHasntBeenImplementedException extends BadRequestException {
    public MoveHasntBeenImplementedException(PlayerActionType type) {
        super("Move of type: %s hasn't been implemented yet, sorry!".formatted(type.toString()));
    }
}
