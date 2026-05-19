package de.hexfieldsstudio.hexfieldsdominion.game;

import de.hexfieldsstudio.hexfieldsdominion.SseSender;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.LobbyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GameManager extends SseSender<UUID> {

    private static final int DICE_MIN_VALUE = 1;
    private static final int DICE_MAX_VALUE = 6;

    private final LobbyManager lobbyManager;

    public RollDiceResponse rollDice(UUID gameUUID, String username) throws MatchNotFoundException {
        Match match = lobbyManager.findLobbyByMatch(gameUUID).getMatch();

        Random random = new Random();
        int value1 = random.nextInt(DICE_MAX_VALUE) + DICE_MIN_VALUE;
        int value2 = random.nextInt(DICE_MAX_VALUE) + DICE_MIN_VALUE;

        RollDiceResponse response = new RollDiceResponse(value1, value2);
        match.setCurrentDiceResult(new Integer[]{value1, value2});

        Map<String, SseEmitter> emitters = groupsEmitters.get(gameUUID);
        if (emitters == null) {
            return response;
        }
        Map<String, SseEmitter> emittersWithoutUser = emitters.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(username))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        sendEvent(emittersWithoutUser, "rollDice", response, gameUUID);
        return response;
    }

    @Override
    public SseEmitter subscribe(UUID matchUUID, String username) {
        SseEmitter emitter = createEmitter(username, matchUUID);

        Map<String, SseEmitter> emitters = Map.of(username, emitter);
        sendEvent(emitters, "initialData", "initialData", matchUUID);

        return emitter;
    }

    @Override
    public void onUnsubscribe(UUID matchUUID, String username) {
        try {
            lobbyManager.findLobbyByMatch(matchUUID).removePlayer(username);
        } catch (MatchNotFoundException ignored) {}
    }

    public record RollDiceResponse(int value1, int value2) {}

}
