package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.SseSender;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.error.ForbiddenException;
import de.hexfieldsstudio.hexfieldsdominion.game.board.Structure;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.BuildActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.PlayerActionDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.dto.TradeBankDTO;
import de.hexfieldsstudio.hexfieldsdominion.game.error.InvalidBuildRequestException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MoveHasntBeenImplementedException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotPlayersTurnException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
import de.hexfieldsstudio.hexfieldsdominion.game.types.ResourceType;
import de.hexfieldsstudio.hexfieldsdominion.lobby.LobbyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.SecureRandom;
import java.util.*;

@Component
@RequiredArgsConstructor
public class GameManager extends SseSender<UUID> {

    private static final int DICE_MIN_VALUE = 1;
    private static final int DICE_MAX_VALUE = 6;
    private static final int POINTS_REQUIRED_TO_WIN = 10;

    private final LobbyManager lobbyManager;

    public void rollDice(UUID gameUUID, User user) throws MatchNotFoundException, NotPlayersTurnException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();
        if (!match.getPlayers().isPlayersTurn(user)) {
            throw new NotPlayersTurnException();
        }

        if (match.isRolledDiceThisTurn()) {
            throw new ForbiddenException("Already rolled dice.");
        }
        match.setRolledDiceThisTurn(true);

        SecureRandom random = new SecureRandom();
        int value1 = random.nextInt(DICE_MAX_VALUE) + DICE_MIN_VALUE;
        int value2 = random.nextInt(DICE_MAX_VALUE) + DICE_MIN_VALUE;

        match.setCurrentDiceResult(new Integer[]{value1, value2});

        sendEvent(allEmitters(gameUUID), "rollDice", new MatchData(match), gameUUID);

        // add resources. Updating will happen on client request so it doesn't happen during the dice animation
        match.grantResourcesForDiceResult(value1 + value2);
    }

    public void nextPlayersTurn(UUID gameUUID, User user) throws MatchNotFoundException, NotPlayersTurnException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();
        if (!match.getPlayers().isPlayersTurn(user)) {
            throw new NotPlayersTurnException();
        }

        match.nextPlayersTurn();

        sendMatchData(allEmitters(gameUUID), match);
    }

    public void addPoints(UUID gameUUID, User user, int points) throws MatchNotFoundException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();

        match.getPlayers().getPlayerForUser(user).ifPresent(player -> {
            player.addPoints(points);

            if (player.getPoints() >= POINTS_REQUIRED_TO_WIN) {
                match.getPlayers().setWinner(player);
            }

            sendMatchData(allEmitters(gameUUID), match);
        });
    }

    public void handlePlayerAction(UUID gameUUID, User user, PlayerActionDTO request)
            throws InvalidBuildRequestException, MoveHasntBeenImplementedException {
        switch (request.getType()){
            case BUILD -> {
                Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();
                if (!match.getPlayers().isPlayersTurn(user)) {
                    throw new NotPlayersTurnException();
                }
                BuildActionDTO buildActionDTO = (BuildActionDTO) request;
                buildBuilding(user, match, buildActionDTO);
                sendMatchData(allEmitters(gameUUID), match);
            }
            case TRADE_BANK -> {
                Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();
                if (!match.getPlayers().isPlayersTurn(user)) {
                    throw new NotPlayersTurnException();
                }
                TradeBankDTO tradeBankDTO = (TradeBankDTO) request;
                match.getTradingHandler().tradeBank(user, match, tradeBankDTO);
                sendMatchData(allEmitters(gameUUID), match);
            }
            default -> throw new MoveHasntBeenImplementedException(request.getType());
        }
    }

    public void buildBuilding(User user, Match match, BuildActionDTO buildActionDTO) throws InvalidBuildRequestException{
        if(!match.getValidator().validate(user, match, buildActionDTO)) throw new InvalidBuildRequestException();
        match.buildBuilding(user, buildActionDTO);
    }

    public Optional<Map<ResourceType, Integer>> getGrantedResources(UUID gameUUID, User user) throws MatchNotFoundException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();

        sendMatchData(emittersOfOnly(gameUUID, user.getUsername()), match);

        return match.getPlayers().getPlayerForUser(user)
                .map(player -> match.getGrantedResourcesThisTurn().get(player.getPublicId()));
    }

    @Override
    public SseEmitter subscribe(UUID gameUUID, String username) {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();

        SseEmitter emitter = createEmitter(username, gameUUID);

        sendMatchData(emittersOfOnly(gameUUID, username), match);

        return emitter;
    }

    @Override
    public void onUnsubscribe(UUID matchUUID, String username) {
        try {
            lobbyManager.findLobbyByMatch(matchUUID).removePlayer(username);
        } catch (MatchNotFoundException ignored) {}
    }

    private void sendMatchData(Map<String, SseEmitter> emitters, Match match) {
        sendEvent(emitters, "matchData", new MatchData(match), match.getUuid());
    }

    private record MatchData(
            List<Structure> structures,
            List<PlayerRepresentation> players,
            int playerCurrentTurn,
            Integer[] currentDiceResult,
            boolean rolledDiceThisTurn,
            PlayerRepresentation winner
    ) {
        public MatchData(Match match) {
            this(
                match.getGameBoard().getStructures(),
                match.getPlayers().getPlayers(),
                match.getPlayers().getPlayerCurrentTurn(),
                match.getCurrentDiceResult(),
                match.isRolledDiceThisTurn(),
                match.getPlayers().getWinner()
            );
        }
    }

}
