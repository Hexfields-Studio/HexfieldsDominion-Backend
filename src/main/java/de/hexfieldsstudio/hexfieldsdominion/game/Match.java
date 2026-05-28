package de.hexfieldsstudio.hexfieldsdominion.game;

import java.security.SecureRandom;
import java.util.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import lombok.Getter;
import lombok.Setter;


public class Match {

    @Getter
    private final List<PlayerRepresentation> players;
    @Getter
    private final List<Field> fields;
    private List<Structure> structures;
    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private Integer[] currentDiceResult = null;
    @Getter
    private final List<Integer> playersTurnOrder;

    public Match(UUID uuid, int boardRadius, Lobby lobby){
        this.uuid = uuid;

        Map<ResourceType, Float> ratios = Map.of(
                ResourceType.WOOD, 0.3f,
                ResourceType.BRICK, 0.2f,
                ResourceType.WHEAT, 0.3f,
                ResourceType.SHEEP, 0.2f
        );
        this.fields = FieldFactory.generateFields(boardRadius, ratios);

        this.players = this.createPlayerRepresentationsForLobby(lobby);
        this.playersTurnOrder = this.generatePlayersTurnOrder();
    }

    private List<PlayerRepresentation> createPlayerRepresentationsForLobby(Lobby lobby) {
        return lobby.getPlayers().stream()
                .map(PlayerRepresentation::new)
                .toList();
    }

    private List<Integer> generatePlayersTurnOrder() {
        List<Integer> idsList = new ArrayList<>(players.stream()
                .map(PlayerRepresentation::getPublicId)
                .toList());
        Collections.shuffle(idsList);
        return idsList;
    }

    public void nextPlayersTurn() {
        playersTurnOrder.add(playersTurnOrder.removeFirst());
    }

    public int getPlayerCurrentTurn() {
        return playersTurnOrder.getFirst();
    }

    public boolean isPlayersTurn(User user) {
        return this.getPlayerForUser(user)
                .map(player -> player.getUsername().equals(user.getUsername()))
                .orElse(false);
    }

    public Optional<PlayerRepresentation> getPlayerForUser(User user) {
        return players.stream()
                .filter(player -> player.getUsername().equals(user.getUsername()))
                .findFirst();
    }

}
