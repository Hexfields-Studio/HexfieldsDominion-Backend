package de.hexfieldsstudio.hexfieldsdominion.game;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PickDicePairDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PlayerActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradePlayerDTO;

@RestController
@RequestMapping(path = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
public class GameController {
    
    @PostMapping("/{gameUUID}")
    private void playerAction(PlayerActionDTO request) {

    }
    
    @PostMapping("/{gameUUID}")
    private void endTurn() {

    }

    private void buildStructure(BuildActionDTO dto) {

    }

    private void tradeWithBank(TradeBankDTO dto) {

    }

    private void tradeWithPlayer(TradePlayerDTO dto) {

    }

    private void pickDicePair(PickDicePairDTO dto) {
        
    }
}