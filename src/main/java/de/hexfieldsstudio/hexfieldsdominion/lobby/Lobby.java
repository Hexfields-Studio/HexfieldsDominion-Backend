package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.ArrayList;
import java.util.List;

import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import lombok.Getter;

@Getter
public class Lobby {

    List<Player> players = new ArrayList<>();

    public void addPlayer(Player player){
        //TODO: Add a safe check to make sure no player gets added twice
        players.add(player);
    }
}