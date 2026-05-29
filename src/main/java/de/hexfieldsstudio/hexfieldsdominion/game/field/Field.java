package de.hexfieldsstudio.hexfieldsdominion.game.field;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Field (AxialPosition pos, int numberChip, ResourceType resource) {
    public static Map<AxialPosition, Field> getFieldsMap(List<Field> fields){
        Map<AxialPosition, Field> map =  new HashMap<>();
        for(Field field : fields){
            map.put(field.pos(), field);
        }
        return map;
    }
}