package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import java.util.Map;

import de.hexfieldsstudio.hexfieldsdominion.game.types.RessourceType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TradePlayerDTO extends PlayerActionDTO{
    private String destPublicId;
    private Map<RessourceType, Integer> offered;
    private Map<RessourceType, Integer> requested;
}