package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;

@Component
public class LobbyManager{

    private final static int HEARTBEAT_INTERVAL_SECONDS = 5;

    private final HashMap<String, Lobby> occupiedLobbies;
    private final List<Lobby> freeLobbies;
    private final Map<String, Map<String, SseEmitter>> lobbyEmitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public LobbyManager(AppConfig config){
        int initialCapacity = config.getInitialCapacity();
        occupiedLobbies = new HashMap<>(initialCapacity);
        freeLobbies = new ArrayList<>();
        for (int i = 0; i < initialCapacity; i++){
            freeLobbies.add(new Lobby());
        }
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeatEvents, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
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

    public SseEmitter subscribeToLobby(String lobbyCode, String username) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        lobbyEmitters.computeIfAbsent(lobbyCode, k -> new ConcurrentHashMap<>()).put(username, emitter);

        emitter.onCompletion(() -> unsubscribeFromLobby(lobbyCode, username));
        emitter.onError((throwable) -> unsubscribeFromLobby(lobbyCode, username));
        emitter.onTimeout(() -> unsubscribeFromLobby(lobbyCode, username));

        // Send initial data
        Lobby lobby = occupiedLobbies.get(lobbyCode);
        if (lobby != null) {
            try {
                List<Player> players = lobby.getPlayers();
                if (players != null) {
                    emitter.send(SseEmitter.event().name("lobbyUpdate").data(players));
                }
            } catch (IOException e) {
                unsubscribeFromLobby(lobbyCode, username);
            }
        }

        return emitter;
    }

    private void unsubscribeFromLobby(String lobbyCode, String username) {
        Map<String, SseEmitter> emitters = lobbyEmitters.get(lobbyCode);
        if (emitters != null) {
            emitters.remove(username);
            if (emitters.isEmpty()) {
                lobbyEmitters.remove(lobbyCode);
            }
        }

        // Remove player from lobby
        Lobby lobby = occupiedLobbies.get(lobbyCode);
        if (lobby != null) {
            lobby.removePlayer(username);
            notifyLobbyUpdate(lobbyCode, lobby.getPlayers());

            // Check if lobby should be marked as free
            checkLobbyCleanup(lobbyCode, lobby);
        }
    }

    private void checkLobbyCleanup(String lobbyCode, Lobby lobby) {
        /* UNUSED CODE 
        List<Player> players = lobby.getPlayers();
        if (players.isEmpty()) {
            // No players left
            if (lobby.isHasAccountPlayer()) {
                // Only if Lobby had registered players, save to DB
                saveLobbyToDatabase(lobbyCode, lobby);
            }
            // Set lobby to free
            occupiedLobbies.remove(lobbyCode);
            freeLobbies.add(lobby);
        }
    }

    private void saveLobbyToDatabase(String lobbyCode, Lobby lobby) {
        // TODO: Implement database save logic
        // This is a stub for saving the lobby/match to database
        // Get the match UUID
        // Upload data to DB ( = persistance)
        System.out.println("Stub: Saving lobby " + lobbyCode + " to database for resumption.");*/
    }

    private void notifyLobbyUpdate(String lobbyCode, List<Player> players) {
        if (players == null) {
            return;
        }
        Map<String, SseEmitter> emitters = lobbyEmitters.get(lobbyCode);
        if (emitters != null) {
            List<String> deadUsers = new ArrayList<>();
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event().name("lobbyUpdate").data(players));
                } catch (IOException e) {
                    deadUsers.add(entry.getKey());
                }
            }
            deadUsers.forEach(username -> unsubscribeFromLobby(lobbyCode, username));
        }
    }

    private void sendHeartbeatEvents() {
        for (Map.Entry<String, Map<String, SseEmitter>> lobbyEntry : lobbyEmitters.entrySet()) {
            String lobbyCode = lobbyEntry.getKey();
            Map<String, SseEmitter> emitters = lobbyEntry.getValue();
            List<String> deadUsers = new ArrayList<>();
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event().name("heartbeat").data("keepalive"));
                } catch (IOException e) {
                    deadUsers.add(entry.getKey());
                }
            }
            deadUsers.forEach(username -> unsubscribeFromLobby(lobbyCode, username));
        }
    }
}
