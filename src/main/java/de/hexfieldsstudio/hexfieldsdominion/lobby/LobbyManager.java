package de.hexfieldsstudio.hexfieldsdominion.lobby;

import de.hexfieldsstudio.hexfieldsdominion.AppConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class LobbyManager{

    private HashMap<String, Lobby> occupiedLobbies = new HashMap<>();
    private HashMap<String, Lobby> freeLobbies = new HashMap<>();

    public LobbyManager(AppConfig config){
        int initialCapacity = config.getInitialCapacity();
        occupiedLobbies = new HashMap<>(initialCapacity);
        freeLobbies = new HashMap<>(initialCapacity);
        for (int i = 0; i < initialCapacity; i++){
            freeLobbies.put(LobbyCodeGenerator.generateCode(), new Lobby());
        }
    }

    public String createLobby(String[] configs) throws Exception {
        Iterator<Map.Entry<String, Lobby>> freeLobbiesIterator = freeLobbies.entrySet().iterator();

        if (freeLobbiesIterator.hasNext()){
            Map.Entry<String, Lobby> entry = freeLobbiesIterator.next();
            String lobbyCode = entry.getKey();
            Lobby lobby = entry.getValue();
            freeLobbies.remove(lobbyCode);
            occupiedLobbies.put(lobbyCode, lobby);
            //TODO: Apply configs here to lobby.
            return lobbyCode;
        }else {
            throw new Exception("Server Capacity has been reached. Could not create lobby.");
        }
    }
}