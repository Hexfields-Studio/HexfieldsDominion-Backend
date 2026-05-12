package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.*;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.lobby.heartbeat.HeartbeatHandler;
import de.hexfieldsstudio.hexfieldsdominion.lobby.heartbeat.NoHeartbeatListener;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Lobby implements NoHeartbeatListener {

    private final HeartbeatHandler heartbeatHandler = new HeartbeatHandler(this);
    private final List<Player> players = new ArrayList<>();
    private boolean hasAccountPlayer = false;
    private int nextPlayerId = 0;
    @Setter
    private String lobbyCode;

    public Player addPlayer(User user, LobbyManager lobbyManager){
        // Check if player already exists before adding
        Optional<Player> existingPlayerOptional = players.stream().filter(p -> p.getUsername().equals(user.getUsername())).findFirst();
        if (existingPlayerOptional.isPresent()) {
            Player existingPlayer = existingPlayerOptional.get();
            heartbeatHandler.resetTimer(existingPlayer.getId());
            // we can't reuse the connection for the same username
            lobbyManager.subscribeToLobby(lobbyCode, existingPlayer.getUsername());
            return existingPlayer;
        }

        Player player = new Player(user, nextPlayerId++);
        players.add(player);
        if (player.isAccount()) {
            hasAccountPlayer = true;
        }
        heartbeatHandler.resetTimer(player.getId());

        heartbeatHandler.registerNoHeartbeat(player, this);
        return player;
    }

    public void removePlayer(String username) {
        players.removeIf(p -> p.getUsername().equals(username));
    }

    public void removePlayer(int id) {
        players.removeIf(p -> p.getId() == id);
    }

    @Override
    public void onNoHeartbeat(Lobby lobby, int playerId) {
        this.removePlayer(playerId);
    }
}