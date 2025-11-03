package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.hexfieldsstudio.hexfieldsdominion.game.types.PlayerActionType;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,          // use the "name" to distinguish types
    include = JsonTypeInfo.As.PROPERTY,  // look for a field in JSON
    property = "type"                    // the field name to look at
)

@JsonSubTypes({
    @JsonSubTypes.Type(value = TradePlayerDTO.class, name = "trade_player"),
    @JsonSubTypes.Type(value = TradeBankDTO.class, name = "trade_bank"),
    @JsonSubTypes.Type(value = BuildActionDTO.class, name = "build"),
    @JsonSubTypes.Type(value = PickDicePairDTO.class, name = "pick_dice_pair"),
})

public abstract class PlayerActionDTO {

    private PlayerActionType type;
    private String sessionId;

}