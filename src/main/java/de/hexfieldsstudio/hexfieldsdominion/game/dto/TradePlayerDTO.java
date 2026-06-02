package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import java.util.Map;

import de.hexfieldsstudio.hexfieldsdominion.game.trading.TradingTarget;
import de.hexfieldsstudio.hexfieldsdominion.game.types.TradingStatus;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TradePlayerDTO extends PlayerActionDTO {
    private Integer id;
    @Getter
    private final TradingStatus status;
    private final TradingTarget target;
    private final Map<ResourceType, Integer> offered;
    private final Map<ResourceType, Integer> requested;
}