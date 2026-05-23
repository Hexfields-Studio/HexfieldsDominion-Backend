package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RadiusTooLargeException extends RuntimeException {
    private final int boardRadius;

    @Override
    public String getMessage() {
        return "error: Radius of %s is too large. The upper limit is set to 6.".formatted(boardRadius);
    }
}
