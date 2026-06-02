package de.hexfieldsstudio.hexfieldsdominion.game.trading;

import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.TradingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class PlayerTrade {
    private int id;
    private Integer predecessorId;
    @Setter
    private TradingStatus status;
    @Setter
    private TradingTarget target;
    private int createdBy;
    @Setter
    private Map<ResourceType, Integer> offered;
    @Setter
    private Map<ResourceType, Integer> requested;
}