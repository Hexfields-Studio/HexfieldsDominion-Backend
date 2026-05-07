package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.hexfieldsstudio.hexfieldsdominion.account.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/lobbies")
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyManager lobbyManager;

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

    @GetMapping("/{lobbyCode}")
    public ResponseEntity<Map<String, Object>> joinLobby(@PathVariable String lobbyCode, @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> res = new HashMap<>();

        User user = AuthUtils.getAuthenticatedUser();

        // Create Player from User
        Player player = new Player();
        player.setUsername(user.getUsername());
        player.setAccount(user.getRole() == Role.PLAYER);

        if (lobbyManager.joinLobby(lobbyCode, player, res)){
            return ResponseEntity.ok(res);
        } else {
            res.put("error", "Lobby with code " + lobbyCode + " not found.");
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/{lobbyCode}/events")
    public SseEmitter lobbyEvents(@PathVariable String lobbyCode) {
        User user = AuthUtils.getAuthenticatedUser();

        return lobbyManager.subscribeToLobby(lobbyCode, user.getUsername());
    }

}