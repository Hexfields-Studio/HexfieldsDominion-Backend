package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import java.util.Map;

import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TradeBankDTO extends PlayerActionDTO {
    private Map<ResourceType, Integer> offered;
    private Map<ResourceType, Integer> requested;
}