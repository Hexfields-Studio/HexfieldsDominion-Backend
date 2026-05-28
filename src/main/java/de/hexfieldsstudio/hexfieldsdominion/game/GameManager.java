package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.SseSender;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.error.ForbiddenException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.NotPlayersTurnException;
import de.hexfieldsstudio.hexfieldsdominion.game.player.PlayerRepresentation;
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

    private final LobbyManager lobbyManager;

    public void rollDice(UUID gameUUID, User user) throws MatchNotFoundException, NotPlayersTurnException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();
        if (!match.isPlayersTurn(user)) {
            throw new NotPlayersTurnException();
        }

        if (match.isRolledDiceThisTurn()) {
            throw new ForbiddenException("Already rolled dice.");
        }
        match.setRolledDiceThisTurn(true);

        SecureRandom random = new SecureRandom();
        int value1 = random.nextInt(DICE_MAX_VALUE) + DICE_MIN_VALUE;
        int value2 = random.nextInt(DICE_MAX_VALUE) + DICE_MIN_VALUE;

        RollDiceResponse response = new RollDiceResponse(value1, value2);
        match.setCurrentDiceResult(new Integer[]{value1, value2});

        // show dices above endTurn button
        sendEvent(allEmittersExcept(gameUUID, user), "rollDice", response, gameUUID);

        sendMatchData(allEmitters(gameUUID), match);
    }

    public void nextPlayersTurn(UUID gameUUID, User user) throws MatchNotFoundException, NotPlayersTurnException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();
        if (!match.isPlayersTurn(user)) {
            throw new NotPlayersTurnException();
        }

        match.nextPlayersTurn();

        sendMatchData(allEmitters(gameUUID), match);
    }

    public void addPoints(UUID gameUUID, User user, int points) throws MatchNotFoundException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();

        match.getPlayerForUser(user).ifPresent(player -> {
            player.addPoints(points);

            sendMatchData(allEmitters(gameUUID), match);
        });
    }

    @Override
    public SseEmitter subscribe(UUID gameUUID, String username) {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();

        SseEmitter emitter = createEmitter(username, gameUUID);

        sendMatchData(emittersOfOnly(username, emitter), match);

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

    public record RollDiceResponse(int value1, int value2) {}

    private record MatchData(List<PlayerRepresentation> players, int playerCurrentTurn, Integer[] currentDiceResult) {
        public MatchData(Match match) {
            this(match.getPlayers(), match.getPlayerCurrentTurn(), match.getCurrentDiceResult());
        }
    }

}
