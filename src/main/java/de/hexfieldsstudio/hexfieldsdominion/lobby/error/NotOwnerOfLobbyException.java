package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

import de.hexfieldsstudio.hexfieldsdominion.error.ForbiddenException;

public class NotOwnerOfLobbyException extends ForbiddenException {
    public NotOwnerOfLobbyException() {
        super("Das kann nur der LobbyOwner tun.");
    }
}
