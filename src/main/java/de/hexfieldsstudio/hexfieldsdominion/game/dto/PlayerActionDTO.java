package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.hexfieldsstudio.hexfieldsdominion.game.types.PlayerActionType;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,          // use the "name" to distinguish types
    include = JsonTypeInfo.As.PROPERTY,  // look for a field in JSON
    property = "type",                    // the field name to look at
    visible = true                       // do not ignore the "type" field => use setType() also
)

@JsonSubTypes({
    @JsonSubTypes.Type(value = TradePlayerDTO.class, name = "TRADE_PLAYER"),
    @JsonSubTypes.Type(value = TradeBankDTO.class, name = "TRADE_BANK"),
    @JsonSubTypes.Type(value = BuildActionDTO.class, name = "BUILD"),
    @JsonSubTypes.Type(value = PickDicePairDTO.class, name = "PICK_DICE_PAIR"),
})

public abstract class PlayerActionDTO {

    private PlayerActionType type;
    private String sessionId;

    public void setType(PlayerActionType type){
        this.type = type;
    }

    public void setSessionId(String sessionId){
        this.sessionId = sessionId;
    }

    public PlayerActionType getType(){
        return type;
    }

     public String getSessionId(){
        return sessionId;
    }
    
}
