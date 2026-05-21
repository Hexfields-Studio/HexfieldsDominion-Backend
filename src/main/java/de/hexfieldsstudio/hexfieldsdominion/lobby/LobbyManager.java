package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.*;

import de.hexfieldsstudio.hexfieldsdominion.SseSender;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.LobbyNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.NotOwnerOfLobbyException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.heartbeat.NoHeartbeatListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;

@Component
public class LobbyManager extends SseSender<String> implements NoHeartbeatListener {

    private final HashMap<String, Lobby> occupiedLobbies;
    private final List<Lobby> freeLobbies;

    public LobbyManager(AppConfig config) {
        int initialCapacity = config.getInitialCapacity();
        occupiedLobbies = new HashMap<>(initialCapacity);
        freeLobbies = new ArrayList<>();
        for (int i = 0; i < initialCapacity; i++){
            freeLobbies.add(new Lobby(config));
        }
    }

    public String createLobby(String[] configs, String owner) throws Exception {
        if (!freeLobbies.isEmpty()){
            Lobby lobby = freeLobbies.removeFirst();
            lobby.setOwner(owner);
            String lobbyCode = LobbyCodeGenerator.generateCode();
            lobby.setLobbyCode(lobbyCode);
            occupiedLobbies.put(lobbyCode, lobby);
            //TODO: Apply configs here to lobby.
            return lobbyCode;
        } else {
            throw new Exception("Server Capacity has been reached. Could not create lobby.");
        }
    }

    public JoinedLobbyResponse joinLobby(String lobbyCode, User user) throws LobbyNotFoundException {
        Lobby lobby = this.findOccupiedLobbyOrThrow(lobbyCode);
        Player player = lobby.addPlayer(user, this);
        notifyLobbyUpdate(lobby);

        lobby.getHeartbeatHandler().registerNoHeartbeat(player, this);
        return new JoinedLobbyResponse(player, lobby);
    }

    public Lobby findOccupiedLobbyOrThrow(String lobbyCode) throws LobbyNotFoundException {
        if (!occupiedLobbies.containsKey(lobbyCode)) {
            throw new LobbyNotFoundException(lobbyCode);
        }
        return occupiedLobbies.get(lobbyCode);
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

    private void notifyLobbyUpdate(Lobby lobby) {
        String lobbyCode = lobby.getLobbyCode();
        List<Player> players = lobby.getPlayers();

        if (players == null) {
            return;
        }
        sendEvent(allEmitters(lobbyCode), "lobbyUpdate", players, lobbyCode);
    }

    public Match createMatchForLobby(Lobby lobby, User user) throws NotOwnerOfLobbyException {
        if (!lobby.isOwner(user.getUsername())) {
            throw new NotOwnerOfLobbyException();
        }

        // random uuid could be replaced in the future to ensure uniqueness
        Match match = new Match(UUID.randomUUID());
        lobby.setMatch(match);

        sendEvent(allEmittersExcept(lobby.getLobbyCode(), user), "matchCreated", new CreatedMatchResponse(match), lobby.getLobbyCode());
        return match;
    }

    public Lobby findLobbyByMatch(UUID matchUUID) throws MatchNotFoundException {
        return occupiedLobbies.values().stream()
                    .filter(lobby -> matchUUID.equals(lobby.getMatch().getUuid()))
                    .findFirst()
                    .orElseThrow(() -> new MatchNotFoundException(matchUUID));
    }

    @Override
    public SseEmitter subscribe(String lobbyCode, String username) {
        SseEmitter emitter = createEmitter(username, lobbyCode);

        Lobby lobby = occupiedLobbies.get(lobbyCode);
        if (lobby == null) {
            return emitter;
        }
        sendEvent(emittersOfOnly(username, emitter), "lobbyUpdate", lobby.getPlayers(), lobbyCode);

        return emitter;
    }

    @Override
    protected void onUnsubscribe(String lobbyCode, String username) {
        // Remove player from lobby
        Lobby lobby = occupiedLobbies.get(lobbyCode);
        if (lobby != null) {
            lobby.removePlayer(username);
            notifyLobbyUpdate(lobby);

            // Check if lobby should be marked as free
            checkLobbyCleanup(lobbyCode, lobby);
        }
    }

    @Override
    public void onNoHeartbeat(Lobby lobby, int playerId) {
        notifyLobbyUpdate(lobby);
    }

    public record CreatedMatchResponse(String matchUUID) {
        public CreatedMatchResponse(Match match) {
            this(match.getUuid().toString());
        }
    }

    public record JoinedLobbyResponse(CreatedPlayer createdPlayer, boolean isLobbyOwner) {
        public JoinedLobbyResponse(Player player, Lobby lobby) {
            this(new CreatedPlayer(player), lobby.isOwner(player.getUsername()));
        }

        private record CreatedPlayer(String username, int id, boolean isAccount) {
            public CreatedPlayer(Player player) {
                this(player.getUsername(), player.getId(), player.isAccount());
            }
        }
    }

}
