package de.hexfieldsstudio.hexfieldsdominion.game.player;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PlayerColorFactory {

    public int generateHueFromUsername(String username) {
        String saltedUsername = username + "@hexfields"; // Add a salt to ensure more variability
        int hash = saltedUsername.hashCode();
        return hash % 360; // Extract hue component (0-360)
    }
}
