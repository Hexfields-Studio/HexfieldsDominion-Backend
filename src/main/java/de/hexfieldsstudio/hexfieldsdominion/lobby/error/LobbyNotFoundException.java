package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LobbyNotFoundException extends Throwable {

    private final String lobbyCode;

    @Override
    public String getMessage() {
        return "error: Lobby with code %s not found.".formatted(lobbyCode);
    }
}
