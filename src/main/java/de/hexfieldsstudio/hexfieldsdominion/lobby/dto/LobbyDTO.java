package de.hexfieldsstudio.hexfieldsdominion.lobby.dto;

import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;

public record LobbyDTO (int lobbyId, Player[] players) {}
