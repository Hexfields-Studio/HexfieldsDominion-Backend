package de.hexfieldsstudio.hexfieldsdominion.lobby.heartbeat;

import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;

public interface NoHeartbeatListener {

    void onNoHeartbeat(Lobby lobby, int playerId);

}
