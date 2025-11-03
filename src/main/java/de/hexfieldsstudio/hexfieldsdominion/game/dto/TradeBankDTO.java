package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import java.util.Map;

import de.hexfieldsstudio.hexfieldsdominion.game.types.RessourceType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TradeBankDTO extends PlayerActionDTO{
    private Map<RessourceType, Integer> offered;
    private Map<RessourceType, Integer> requested;
}