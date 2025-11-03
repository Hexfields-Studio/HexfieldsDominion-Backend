package de.hexfieldsstudio.hexfieldsdominion.game.player;

import de.hexfieldsstudio.hexfieldsdominion.game.Resource;
import lombok.Getter;
import lombok.Setter;
import java.awt.Color;
import java.util.Map;

@Setter
@Getter
public class PlayerRepresentation {
    
    private Player player;
    private int publicId;
    private String sessionId;
    private Color color;
    private Map<Resource, Integer> resources;
    
}
