package de.hexfieldsstudio.hexfieldsdominion.game;

import org.springframework.web.bind.annotation.*;

import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PickDicePairDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PlayerActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradePlayerDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/games")
public class GameController {

    private List<Match> matches;

    @PostMapping("/{gameUUID}/endTurn")
    private void endTurn() {

    }

    @PostMapping("/{gameUUID}/makeMove")
    private void playerAction(@PathVariable UUID gameUUID,
                              @RequestBody PlayerActionDTO request
    ) {

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