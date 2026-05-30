package de.hexfieldsstudio.hexfieldsdominion.game;

import java.util.*;

import de.hexfieldsstudio.hexfieldsdominion.game.board.Field;
import de.hexfieldsstudio.hexfieldsdominion.game.board.GameBoard;
import de.hexfieldsstudio.hexfieldsdominion.game.board.GamePlayers;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Structure;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
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

    public Match(UUID uuid, int boardRadius, Lobby lobby) {
        this.uuid = uuid;
        this.gameBoard = new GameBoard(boardRadius);
        this.players = new GamePlayers(lobby);

        this.generateInitialStructures();
        this.grantInitialResources();
    }

    public void nextPlayersTurn() {
        players.rotateNextPlayer();
        rolledDiceThisTurn = false;
    }

    private void generateInitialStructures() {
        // temporary for simulating initial structures
        Structure[] structures = new Structure[]{new Structure(
                StructureType.TOWN,
                List.of(
                        new AxialPosition(1, 1),
                        new AxialPosition(0, 1),
                        new AxialPosition(1, 0)
                ),
                players.getPlayers().getFirst().getPublicId()
        ),
        new Structure(
                StructureType.TOWN,
                List.of(
                        new AxialPosition(-1, 0),
                        new AxialPosition(-1, -1),
                        new AxialPosition(-2, 0)
                ),
                players.getPlayers().getLast().getPublicId()
        )};

        for (Structure structure : structures) {
            if (structure.getPos().contains(new AxialPosition(0, 0))) {
                System.out.println("ERROR: Invalid initial structures generated. Dunes field (0, 0) should not be next to an initial structure as there is no resource to give.");
            }
            gameBoard.addStructure(structure);
        }
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
                        gameBoard.getFieldsAt(structure.getPos()).forEach(field -> {
                            setOrAddResource(player.getResources(), field);
                        });
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

}
