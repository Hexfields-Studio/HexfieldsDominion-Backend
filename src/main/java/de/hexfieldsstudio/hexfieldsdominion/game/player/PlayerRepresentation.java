package de.hexfieldsstudio.hexfieldsdominion.game.player;

import lombok.Getter;
import lombok.Setter;
import java.awt.Color;

@Setter
@Getter
public class PlayerRepresentation {
    
    private Player player;
    private int publicId;
    private int sessionId;
    private Color color;
    
}
