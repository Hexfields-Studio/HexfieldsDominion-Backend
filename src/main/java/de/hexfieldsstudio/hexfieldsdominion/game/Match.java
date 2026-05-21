package de.hexfieldsstudio.hexfieldsdominion.game;

import java.util.List;
import java.util.UUID;

import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@RequiredArgsConstructor
public class Match {

    private List<PlayerRepresentation> players;
    private List<Field> fields;
    private List<Structure> structures;
    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private Integer[] currentDiceResult = null;

}