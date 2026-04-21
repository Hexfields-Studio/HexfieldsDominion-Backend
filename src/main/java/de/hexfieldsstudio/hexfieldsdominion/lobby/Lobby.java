package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.ArrayList;
import java.util.List;

import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import lombok.Getter;

@Getter
public class Lobby {

    List<Player> players = new ArrayList<>();

    public void addPlayer(Player player){
        // Check if player already exists before adding
        boolean exists = players.stream().anyMatch(p -> p.getId() == player.getId());
        if (!exists) {
            players.add(player);
        }
    }
}