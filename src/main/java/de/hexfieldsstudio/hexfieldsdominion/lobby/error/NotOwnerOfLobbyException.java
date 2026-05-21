package de.hexfieldsstudio.hexfieldsdominion.lobby.error;

public class NotOwnerOfLobbyException extends Throwable {

    @Override
    public String getMessage() {
        return "error: Only the owner is allowed to do that.";
    }
}
