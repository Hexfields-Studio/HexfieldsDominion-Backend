package de.hexfieldsstudio.hexfieldsdominion.game.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,          // use the "name" to distinguish types
    include = JsonTypeInfo.As.PROPERTY,  // look for a field in JSON
    property = "type"                    // the field name to look at
)

@JsonSubTypes({
    @JsonSubTypes.Type(value = TradePlayerDTO.class, name = "trade"),
})

public abstract class PlayerActionDTO {
    
}