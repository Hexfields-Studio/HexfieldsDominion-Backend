package de.hexfieldsstudio.hexfieldsdominion.game.player;

import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.Map;

import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlayerRepresentation {
    
    private Player player;
    String username;
    private int publicId;
    private String sessionId;
    private int colorHue;
    private final Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
    private final String chosenPortrait;
    private int points = 0;

    public PlayerRepresentation(Player player) {
        this.player = player;
        this.username = player.getUsername();
        this.publicId = player.getId();
        // temporary
        SecureRandom random = new SecureRandom();
        this.chosenPortrait = (random.nextInt(2) == 0) ? "KingMale" : "ArcherFemale";
        this.colorHue = PlayerColorFactory.generateHueFromUsername(this.username);
    }

    public void addPoints(int points) {
        this.points += points;
    }
    
}
