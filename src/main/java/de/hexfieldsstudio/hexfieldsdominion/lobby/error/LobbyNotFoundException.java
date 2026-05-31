package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

import de.hexfieldsstudio.hexfieldsdominion.error.NotFoundException;

public class LobbyNotFoundException extends NotFoundException {
    public LobbyNotFoundException(String lobbyCode) {
        super("No Lobby with Code %s was found.".formatted(lobbyCode));
    }
}
