package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

import de.hexfieldsstudio.hexfieldsdominion.error.NotFoundException;

public class LobbyNotFoundException extends NotFoundException {
    public LobbyNotFoundException(String lobbyCode) {
        super("Es wurde keine Lobby mit dem Code %s gefunden.".formatted(lobbyCode));
    }
}
