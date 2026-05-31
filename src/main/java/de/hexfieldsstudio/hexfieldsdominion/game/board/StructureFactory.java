package de.hexfieldsstudio.hexfieldsdominion.game.board;

import de.hexfieldsstudio.hexfieldsdominion.game.AxialPosition;
import de.hexfieldsstudio.hexfieldsdominion.game.BuildingABuildingValidator;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.TooLittleSpaceException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.StructureType;

import java.security.SecureRandom;
import java.util.*;

public class StructureFactory {
    public static void randomlyBuildInitialStructures(Match match, BuildingABuildingValidator validator) throws TooLittleSpaceException {
        int attempts = 0;
        Set<List<AxialPosition>> corners = Set.copyOf(validator.getCorners());
        for (PlayerRepresentation player: match.getPlayers().getPlayers()){
            for (int i = 0; i < 2; i++){    // Place for each player two "TOWN", each with a "STREET"
                if (attempts >= 100) throw new TooLittleSpaceException();
                int rand = new SecureRandom().nextInt(corners.size());
                List<AxialPosition> townPos = new ArrayList<>(corners).get(rand);
                BuildActionDTO town = new BuildActionDTO(StructureType.TOWN, townPos);
                if(!validator.validate(match, town)){
                    i--;
                    attempts++;
                    continue;
                }

                List<AxialPosition> findStreetPos = new ArrayList<>(List.copyOf(townPos));   // cornerPos.size() should be 3
                Collections.rotate(findStreetPos, new SecureRandom().nextInt(3));
                boolean foundValidStreetPos = false;
                BuildActionDTO street = null;
                for (int j = 0; j < findStreetPos.size(); j++){
                    List<AxialPosition> streetPos = List.of(findStreetPos.get(0), findStreetPos.get(1));
                    street = new BuildActionDTO(StructureType.STREET, streetPos);
                    if(validator.validate(match, street)){
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
                structures.add(buildStructureFromDTO(player, town));
                structures.add(buildStructureFromDTO(player, street));
            }
        }
    }

    public static Structure buildStructureFromDTO(PlayerRepresentation player, BuildActionDTO dto){
        return new Structure(dto.getStructureType(), dto.getPos(), player.getPublicId());
    }
}
