package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RadiusTooSmallException extends RuntimeException {

    private final int boardRadius;

    @Override
    public String getMessage() {
        return "error: Radius of %s is too small. It must be set larger or equal to 3.".formatted(boardRadius);
    }
}
