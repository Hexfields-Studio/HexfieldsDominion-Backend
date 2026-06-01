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
    private String colorString;
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
        this.colorString = generateColorFromUsername(this.username);
    }

    private String generateColorFromUsername(String username) {
        int hash = username.hashCode();
        int red = (hash >> 16) % 0xFF; // Extract red component
        int green = (hash >> 8) % 0xFF; // Extract green component
        int blue = hash % 0xFF; // Extract blue component (modulo to ensure it's within 0-255)
        
        return String.format("#%02x%02x%02x", red, green, blue); //convert to hex
    }

    public void addPoints(int points) {
        this.points += points;
    }
    
}
