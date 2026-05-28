package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.account.AuthUtils;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotPlayersTurnException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import de.hexfieldsstudio.hexfieldsdominion.lobby.LobbyManager;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PickDicePairDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PlayerActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradePlayerDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/games")
@RequiredArgsConstructor
public class GameController {

    private final LobbyManager lobbyManager;
    private final GameManager gameManager;

    @GetMapping("/{gameUUID}/lobby")
    private LobbyCodeResponse lobby(@PathVariable UUID gameUUID) throws MatchNotFoundException {
        return new LobbyCodeResponse(lobbyManager.findLobbyByMatch(gameUUID));
    }

    @GetMapping("/{gameUUID}/fields")
    public List<Field> fields(@PathVariable UUID gameUUID) throws MatchNotFoundException {
        return lobbyManager.findLobbyByMatch(gameUUID).getMatch().getFields();
    }

    @GetMapping("/{gameUUID}/events")
    public SseEmitter gameEvents(@PathVariable UUID gameUUID) {
        return gameManager.subscribe(gameUUID, AuthUtils.getAuthenticatedUser().getUsername());
    }

    @PostMapping("/{gameUUID}/rollDice")
    public void rollDice(@PathVariable UUID gameUUID) throws MatchNotFoundException, NotPlayersTurnException {
        gameManager.rollDice(gameUUID, AuthUtils.getAuthenticatedUser());
    }

    @PostMapping("/{gameUUID}/endTurn")
    public void endTurn(@PathVariable UUID gameUUID) throws MatchNotFoundException, NotPlayersTurnException {
        gameManager.nextPlayersTurn(gameUUID, AuthUtils.getAuthenticatedUser());
    }

    // temporary, should be done when building siedlung/stadt
    @PostMapping("/{gameUUID}/addPoint")
    public void addPoint(@PathVariable UUID gameUUID) throws MatchNotFoundException, NotPlayersTurnException {
        gameManager.addPoints(gameUUID, AuthUtils.getAuthenticatedUser(), 1);
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

    public record LobbyCodeResponse(String lobbyCode) {
        public LobbyCodeResponse(Lobby lobby) {
            this(lobby.getLobbyCode());
        }
    }
}