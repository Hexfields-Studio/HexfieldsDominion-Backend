package de.hexfieldsstudio.hexfieldsdominion.game.player;

import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import lombok.Getter;
import lombok.Setter;
import java.awt.Color;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class PlayerRepresentation {
    
    private Player player;
    String username;
    private int publicId;
    private String sessionId;
    private Color color;
    private final Map<ResourceType, Integer> resources = new HashMap<>();
    private final String chosenPortrait;
    private int points = 0;

    public PlayerRepresentation(Player player) {
        this.player = player;
        this.username = player.getUsername();
        this.publicId = player.getId();
        // temporary
        SecureRandom random = new SecureRandom();
        this.chosenPortrait = (random.nextInt(2) == 0) ? "KingMale" : "ArcherFemale";
    }

    public void addPoints(int points) {
        this.points += points;
    }
    
}
