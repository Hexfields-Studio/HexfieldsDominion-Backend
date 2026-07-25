package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.BuildingABuildingValidator;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.TooLittleSpaceException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

import java.security.SecureRandom;
import java.util.*;

public class StructureFactory {

    private static final EnumMap<ResourceType, Integer> settlementRecipe = new EnumMap<>(Map.of(
            ResourceType.WOOD, 1,
            ResourceType.BRICK, 1,
            ResourceType.SHEEP, 1,
            ResourceType.WHEAT, 1
    ));
    private static final EnumMap<ResourceType, Integer> streetRecipe =new EnumMap<>(Map.of(
            ResourceType.WOOD,  1,
            ResourceType.BRICK, 1
    ));
    private static final EnumMap<ResourceType, Integer> townRecipe = new EnumMap<>(Map.of(
            ResourceType.BRICK,  4,
            ResourceType.WHEAT,  2
    ));

    public static void randomlyBuildInitialStructures(Match match, BuildingABuildingValidator validator) throws TooLittleSpaceException {
        int attempts = 0;
        Set<List<AxialPosition>> corners = Set.copyOf(validator.getCorners());
        for (PlayerRepresentation player: match.getPlayers().getPlayers()){
            for (int i = 0; i < 2; i++){    // Place for each player two "SETTLEMENT", each with a "STREET"
                if (attempts >= 500) throw new TooLittleSpaceException();
                int rand = new SecureRandom().nextInt(corners.size());
                List<AxialPosition> settlementPos = new ArrayList<>(corners).get(rand);
                BuildActionDTO settlement = new BuildActionDTO(StructureType.SETTLEMENT, settlementPos);

                List<Field> foundFields;
                try {
                    // make sure all nearby fields have resources and are e.g. not next to water
                    foundFields = match.getGameBoard().getFieldsAt(settlementPos);
                } catch (GameBoard.NotAllFieldsFoundException e) {
                    i--;
                    attempts++;
                    continue;
                }
                if(!validator.validate(null, match, settlement) || foundFields.stream().anyMatch(field -> field.resource() == ResourceType.DUNES)){
                    i--;
                    attempts++;
                    continue;
                }

                List<AxialPosition> findStreetPos = new ArrayList<>(List.copyOf(settlementPos));   // cornerPos.size() should be 3
                Collections.rotate(findStreetPos, new SecureRandom().nextInt(3));
                boolean foundValidStreetPos = false;
                BuildActionDTO street = null;
                for (int j = 0; j < findStreetPos.size(); j++){
                    List<AxialPosition> streetPos = List.of(findStreetPos.get(0), findStreetPos.get(1));
                    street = new BuildActionDTO(StructureType.STREET, streetPos);
                    if(validator.validate(null, match, street)){
                        foundValidStreetPos = true;
                        break;
                    }else{
                        Collections.rotate(findStreetPos, 1);
                    }
                }

                if(!foundValidStreetPos){
                    i--;
                    attempts++;
                    continue;
                }

                List<Structure> structures = match.getGameBoard().getStructures();
                structures.add(buildStructureFromDTO(player, settlement));
                structures.add(buildStructureFromDTO(player, street));
            }
        }
    }

    public static EnumMap<ResourceType, Integer> getRecipeForStructureType(StructureType type) {
        switch (type){
            case SETTLEMENT -> {
                return settlementRecipe;
            }
            case STREET -> {
                return streetRecipe;
            }
            case TOWN -> {
                return townRecipe;
            }
        }
        return new EnumMap<>(ResourceType.class);
    }

    public static Structure buildStructureFromDTO(PlayerRepresentation player, BuildActionDTO dto){
        return new Structure(dto.getStructureType(), dto.getPos(), player.getPublicId(),
                getRecipeForStructureType(dto.getStructureType()));
    }
}
