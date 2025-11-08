package de.hexfieldsstudio.hexfieldsdominion.lobby.dto;

import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LobbyDTO {
    private int lobbyId;
    private Player[] players;
}
