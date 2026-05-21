package de.hexfieldsstudio.hexfieldsdominion.game.error;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MatchNotFoundException extends Throwable {

    private final UUID matchUUID;

    @Override
    public String getMessage() {
        return "error: Match with uuid %s not found.".formatted(matchUUID.toString());
    }
}
