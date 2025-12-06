package de.hexfieldsstudio.hexfieldsdominion.lobby;

import de.hexfieldsstudio.hexfieldsdominion.config.AppConfig;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LobbyManager{

    private final HashMap<String, Lobby> occupiedLobbies;
    private final List<Lobby> freeLobbies;

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

    public boolean joinLobby(String lobbyCode, Map<String, Object> res) {
        Lobby lobby = occupiedLobbies.get(lobbyCode);
        if (lobby != null) {
            Player joinedPlayer = new Player();
            //TODO: Replace this demo Player Object
            joinedPlayer.setId(42);
            joinedPlayer.setUsername("Some_Guest");
            joinedPlayer.setAccount(false);

            lobby.addPlayer(joinedPlayer);
            res.put("players", lobby.getPlayers());
            return true;
        }else return false;
    }
}