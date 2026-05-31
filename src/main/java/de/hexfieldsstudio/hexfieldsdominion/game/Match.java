package de.hexfieldsstudio.hexfieldsdominion.game;

import java.util.*;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.board.*;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.TooLittleSpaceException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.board.StructureFactory;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import lombok.Getter;
import lombok.Setter;

public class Match {

    private static final int AMOUNT_GRANTED_RESOURCES_PER_STRUCTURE_AND_FIELD = 1;

    @Getter
    private final UUID uuid;
    @Getter
    private final GameBoard gameBoard;
    @Getter
    private final GamePlayers players;
    @Getter
    private final Map<Integer, Map<ResourceType, Integer>> grantedResourcesThisTurn = new HashMap<>();
    @Getter
    @Setter
    private Integer[] currentDiceResult = null;
    @Setter
    @Getter
    private boolean rolledDiceThisTurn = false;
    @Getter
    private final BuildingABuildingValidator validator;

    public Match(UUID uuid, int boardRadius, Lobby lobby) throws TooLittleSpaceException {
        this.uuid = uuid;

        this.gameBoard = new GameBoard(boardRadius);
        this.validator = new BuildingABuildingValidator(this.gameBoard.getFields());

        this.players = new GamePlayers(lobby);

        this.grantInitialResources();
        StructureFactory.randomlyBuildInitialStructures(this, validator);
    }

    public void nextPlayersTurn() {
        players.rotateNextPlayer();
        rolledDiceThisTurn = false;
    }

    private void grantInitialResources() {
        gameBoard.getStructures().stream()
                .filter(structure -> structure.getType().equals(StructureType.TOWN))
                .forEach(structure -> {
                    Optional<PlayerRepresentation> playerOptional = players.getPlayerById(structure.getOwnerId());
                    if (playerOptional.isEmpty()) {
                        return;
                    }
                    PlayerRepresentation player = playerOptional.get();

                    try {
                        gameBoard.getFieldsAt(structure.getPos()).forEach(field -> setOrAddResource(player.getResources(), field));
                    } catch (GameBoard.NotAllFieldsFoundException e) {
                        System.out.println("ERROR: Invalid initial structures generated. Could not find fields for all AxialPos.");
                    }
                });
    }

    public void grantResourcesForDiceResult(int diceResult) {
        grantedResourcesThisTurn.clear();

        for (Field field : gameBoard.getFieldsByNumberChip(diceResult)) {
            gameBoard.getStructures().stream()
                    .filter(structure -> structure.getType().equals(StructureType.TOWN))
                    .filter(structure -> structure.getPos().contains(field.pos()))
                    .forEach(structure -> {
                        Optional<PlayerRepresentation> playerOptional = players.getPlayerById(structure.getOwnerId());
                        if (playerOptional.isEmpty()) {
                            return;
                        }
                        PlayerRepresentation player = playerOptional.get();

                        setOrAddResource(player.getResources(), field);

                        if (!grantedResourcesThisTurn.containsKey(player.getPublicId())) {
                            grantedResourcesThisTurn.put(player.getPublicId(),
                                    new HashMap<>(Map.of(field.resource(), AMOUNT_GRANTED_RESOURCES_PER_STRUCTURE_AND_FIELD)));
                            return;
                        }
                        setOrAddResource(grantedResourcesThisTurn.get(player.getPublicId()), field);
                    });
        }
    }

    private void setOrAddResource(Map<ResourceType, Integer> resources, Field field) {
        resources.compute(field.resource(),
                (k, v) -> (v == null)
                        ? AMOUNT_GRANTED_RESOURCES_PER_STRUCTURE_AND_FIELD
                        : v + AMOUNT_GRANTED_RESOURCES_PER_STRUCTURE_AND_FIELD
        );
    }

    public void buildBuilding(User user, BuildActionDTO buildActionDTO){
        this.players.getPlayerForUser(user).ifPresentOrElse(
                player -> this.buildBuilding(player, buildActionDTO),
                () -> System.out.println("Player " + user.getUsername() + " not found")
        );
    }

    public void buildBuilding(PlayerRepresentation player, BuildActionDTO buildActionDTO){
        gameBoard.addStructure(player, buildActionDTO);
    }
}
