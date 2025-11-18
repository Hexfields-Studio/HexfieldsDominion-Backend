package de.hexfieldsstudio.hexfieldsdominion.lobby;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LobbyController {

    private final LobbyManager lobbyManager;

    @Autowired
    public LobbyController(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String root() {
        return "OK";
    }

    @PatchMapping(path = "/lobbies",produces = "application/json")
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

    @GetMapping("/{lobbyCode}")
    public ResponseEntity<Map<String, Object>> joinLobby(@PathVariable String lobbyCode) {
        Map<String, Object> res = new HashMap<>();

        if (lobbyManager.joinLobby(lobbyCode, res)){
            return ResponseEntity.ok(res);
        }else{
            res.put("error", "Lobby with code " + lobbyCode + " not found.");
            return ResponseEntity.badRequest().body(res);
        }
    }

}