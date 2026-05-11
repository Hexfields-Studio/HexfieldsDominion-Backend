package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.ArrayList;
import java.util.List;

import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import lombok.Getter;

@Getter
public class Lobby {

    List<Player> players = new ArrayList<>();
    private boolean hasAccountPlayer = false;

    public void addPlayer(Player player){
        // Check if player already exists before adding
        boolean exists = players.stream().anyMatch(p -> p.getUsername().equals(player.getUsername()));
        if (!exists) {
            players.add(player);
            if (player.isAccount()) {
                hasAccountPlayer = true;
            }
        }
    }

    public void removePlayer(String username) {
        players.removeIf(p -> p.getUsername().equals(username));
    }
}