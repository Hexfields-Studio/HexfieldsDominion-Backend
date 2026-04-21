package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;

@Component
public class LobbyManager{

    private final HashMap<String, Lobby> occupiedLobbies;
    private final List<Lobby> freeLobbies;
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> lobbyEmitters = new HashMap<>();

    public LobbyManager(AppConfig config){
        int initialCapacity = config.getInitialCapacity();
        occupiedLobbies = new HashMap<>(initialCapacity);
        freeLobbies = new ArrayList<>();
        for (int i = 0; i < initialCapacity; i++){
            freeLobbies.add(new Lobby());
        }
    }

    public String createLobby(String[] configs) throws Exception {
        if (!freeLobbies.isEmpty()){
            Lobby lobby = freeLobbies.removeFirst();
            String lobbyCode = LobbyCodeGenerator.generateCode();
            occupiedLobbies.put(lobbyCode, lobby);
            //TODO: Apply configs here to lobby.
            return lobbyCode;
        }else {
            throw new Exception("Server Capacity has been reached. Could not create lobby.");
        }
    }

    public boolean joinLobby(String lobbyCode, Player player, Map<String, Object> res) {
        Lobby lobby = occupiedLobbies.get(lobbyCode);
        if (lobby != null) {
            lobby.addPlayer(player);
            res.put("players", lobby.getPlayers());
            notifyLobbyUpdate(lobbyCode, lobby.getPlayers());
            return true;
        }else return false;
    }

    public SseEmitter subscribeToLobby(String lobbyCode) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        lobbyEmitters.computeIfAbsent(lobbyCode, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> unsubscribeFromLobby(lobbyCode, emitter));
        emitter.onError((throwable) -> unsubscribeFromLobby(lobbyCode, emitter));
        emitter.onTimeout(() -> unsubscribeFromLobby(lobbyCode, emitter));

        // Send initial data
        Lobby lobby = occupiedLobbies.get(lobbyCode);
        if (lobby != null) {
            try {
                List<Player> players = lobby.getPlayers();
                if (players != null) {
                    emitter.send(SseEmitter.event().name("lobbyUpdate").data(players));
                }
            } catch (IOException e) {
                unsubscribeFromLobby(lobbyCode, emitter);
            }
        }

        return emitter;
    }

    private void unsubscribeFromLobby(String lobbyCode, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = lobbyEmitters.get(lobbyCode);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                lobbyEmitters.remove(lobbyCode);
            }
        }
    }

    private void notifyLobbyUpdate(String lobbyCode, List<Player> players) {
        if (players == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> emitters = lobbyEmitters.get(lobbyCode);
        if (emitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("lobbyUpdate").data(players));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }
            deadEmitters.forEach(emitter -> unsubscribeFromLobby(lobbyCode, emitter));
        }
    }
}