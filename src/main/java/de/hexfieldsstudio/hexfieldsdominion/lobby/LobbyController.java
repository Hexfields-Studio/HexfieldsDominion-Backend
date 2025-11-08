package de.hexfieldsstudio.hexfieldsdominion.lobby;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;

import java.util.HashMap;
import java.util.Map;

/*
Code eingeben und wenn logged in auf lobby
1. Authentizierung (Skip)
2. Lobby suchen und zurücksenden
* */
@Slf4j
@RestController
@RequestMapping(path = "/lobbies")
public class LobbyController {

    private LobbyManager lobbyManager;

    @Autowired
    public LobbyController(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @PatchMapping(produces = "application/json")
    public ResponseEntity<Map<String, String>> createLobby(@RequestBody(required = false) CreateLobbyDTO configs) {
        Map<String, String> res = new HashMap<>();
        try{
            String lobbyCode = lobbyManager.createLobby(
                    (configs != null)
                    ? configs.getConfigs()
                    : new String[0]
            );
            res.put("lobbyCode", lobbyCode);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @GetMapping("/{code}")
    public void joinLobby(@PathVariable String code) {

    }

}