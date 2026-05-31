package de.hexfieldsstudio.hexfieldsdominion.game.player;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GamePlayers {

    @Getter
    private final List<PlayerRepresentation> players;
    @Getter
    private final List<Integer> playersTurnOrder;
    @Getter
    @Setter
    private PlayerRepresentation winner;

    public GamePlayers(Lobby lobby) {
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

    public void rotateNextPlayer() {
        playersTurnOrder.add(playersTurnOrder.removeFirst());
    }

    public int getPlayerCurrentTurn() {
        return playersTurnOrder.getFirst();
    }

    public boolean isPlayersTurn(User user) {
        return this.getPlayerForUser(user)
                .map(player -> player.getPublicId() == getPlayerCurrentTurn())
                .orElse(false);
    }

    public Optional<PlayerRepresentation> getPlayerForUser(User user) {
        if(user == null) return Optional.empty();
        return players.stream()
                .filter(player -> player.getUsername().equals(user.getUsername()))
                .findFirst();
    }

    public Optional<PlayerRepresentation> getPlayerById(int id) {
        return players.stream()
                .filter(player -> player.getPublicId() == id)
                .findFirst();
    }

}
