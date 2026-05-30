package de.hexfieldsstudio.hexfieldsdominion.game;

import java.util.*;

import de.hexfieldsstudio.hexfieldsdominion.game.board.GameBoard;
import de.hexfieldsstudio.hexfieldsdominion.game.board.GamePlayers;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Structure;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
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
    private final GamePlayers gamePlayers;
    @Getter
    @Setter
    private Integer[] currentDiceResult = null;
    @Setter
    @Getter
    private boolean rolledDiceThisTurn = false;

    public Match(UUID uuid, int boardRadius, Lobby lobby) {
        this.uuid = uuid;
        this.gameBoard = new GameBoard(boardRadius);
        this.gamePlayers = new GamePlayers(lobby);

        this.generateInitialStructures();
        this.grantInitialResources();
    }

    public void nextPlayersTurn() {
        gamePlayers.rotateNextPlayer();
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
                gamePlayers.getPlayers().getFirst().getPublicId()
        ),
        new Structure(
                StructureType.TOWN,
                List.of(
                        new AxialPosition(-1, 0),
                        new AxialPosition(-1, -1),
                        new AxialPosition(-2, 0)
                ),
                gamePlayers.getPlayers().getLast().getPublicId()
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
                    Optional<PlayerRepresentation> playerOptional = gamePlayers.getPlayerById(structure.getOwnerId());
                    if (playerOptional.isEmpty()) {
                        return;
                    }
                    PlayerRepresentation player = playerOptional.get();

                    try {
                        gameBoard.getFieldsAt(structure.getPos()).forEach(field -> {
                            player.getResources().compute(field.resource(),
                                    (k, v) -> (v == null)
                                            ? AMOUNT_GRANTED_RESOURCES_PER_STRUCTURE_AND_FIELD
                                            : v + AMOUNT_GRANTED_RESOURCES_PER_STRUCTURE_AND_FIELD
                            );
                        });
                    } catch (GameBoard.NotAllFieldsFoundException e) {
                        System.out.println("ERROR: Invalid initial structures generated. Could not find fields for all AxialPos.");
                    }
                });
    }

}
