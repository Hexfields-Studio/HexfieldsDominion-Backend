package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.account.AuthUtils;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Field;
import de.hexfieldsstudio.hexfieldsdominion.game.error.InvalidBuildRequestException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MoveHasntBeenImplementedException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotPlayersTurnException;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.Lobby;
import de.hexfieldsstudio.hexfieldsdominion.lobby.LobbyManager;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PickDicePairDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PlayerActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradePlayerDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

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
        return lobbyManager.findLobbyByMatch(gameUUID).getMatch().getGameBoard().getFields();
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

    @GetMapping("/{gameUUID}/grantedResources")
    public Map<ResourceType, Integer> grantedResources(@PathVariable UUID gameUUID, HttpServletResponse response) throws MatchNotFoundException, NotPlayersTurnException {
        Optional<Map<ResourceType, Integer>> resourcesOptional = gameManager.getGrantedResources(gameUUID, AuthUtils.getAuthenticatedUser());
        if (resourcesOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return Collections.emptyMap();
        }
        return resourcesOptional.get();
    }

    @PostMapping("/{gameUUID}/makeMove")
    private void playerAction(@PathVariable UUID gameUUID,
                              @RequestBody PlayerActionDTO request
    ) throws InvalidBuildRequestException, MoveHasntBeenImplementedException {
        gameManager.handlePlayerAction(gameUUID, AuthUtils.getAuthenticatedUser(), request);
    }

    public record LobbyCodeResponse(String lobbyCode) {
        public LobbyCodeResponse(Lobby lobby) {
            this(lobby.getLobbyCode());
        }
    }
}