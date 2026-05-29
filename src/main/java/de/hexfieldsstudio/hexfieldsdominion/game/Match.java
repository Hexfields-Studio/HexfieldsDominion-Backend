package de.hexfieldsstudio.hexfieldsdominion.game;

import java.util.*;
import java.util.List;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.TooLittleSpaceException;
import de.hexfieldsstudio.hexfieldsdominion.game.field.Field;
import de.hexfieldsstudio.hexfieldsdominion.game.field.FieldFactory;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.structure.Structure;
import de.hexfieldsstudio.hexfieldsdominion.game.structure.StructureFactory;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


public class Match {

    @Getter
    private final List<PlayerRepresentation> players;
    @Getter
    private final List<Field> fields;
    @Getter
    @NonNull
    private final List<Structure> structures = new ArrayList<>();
    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private Integer[] currentDiceResult = null;
    @Getter
    private final List<Integer> playersTurnOrder;
    @Setter
    @Getter
    private boolean rolledDiceThisTurn = false;
    @Getter
    private final BuildingABuildingValidator validator;

    public Match(UUID uuid, int boardRadius, Lobby lobby) throws TooLittleSpaceException {
        this.uuid = uuid;

        Map<ResourceType, Float> ratios = Map.of(
                ResourceType.WOOD, 0.3f,
                ResourceType.BRICK, 0.2f,
                ResourceType.WHEAT, 0.3f,
                ResourceType.SHEEP, 0.2f
        );
        this.fields = FieldFactory.generateFields(boardRadius, ratios);
        this.validator = new BuildingABuildingValidator(this.fields);

        this.players = this.createPlayerRepresentationsForLobby(lobby);
        this.playersTurnOrder = this.generatePlayersTurnOrder();

        StructureFactory.randomlyBuildInitialStructures(this, validator);
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
        Collections.rotate(playersTurnOrder, 1);
        rolledDiceThisTurn = false;
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
        return players.stream()
                .filter(player -> player.getUsername().equals(user.getUsername()))
                .findFirst();
    }

    public void buildBuilding(User user, BuildActionDTO buildActionDTO){
        this.getPlayerForUser(user).ifPresentOrElse(player -> {
            this.buildBuilding(player, buildActionDTO);
        }, () -> {
            System.out.println("Player " + user.getUsername() + " not found");
        });
    }

    public void buildBuilding(PlayerRepresentation player, BuildActionDTO buildActionDTO){
        structures.add(StructureFactory.buildStructureFromDTO(player, buildActionDTO));
    }
}
