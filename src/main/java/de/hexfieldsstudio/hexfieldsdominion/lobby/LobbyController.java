package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.hexfieldsstudio.hexfieldsdominion.account.token.JwtService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.AllUserRepository;
import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.player.Player;
import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/lobbies")
@RestController
public class LobbyController {

    private final LobbyManager lobbyManager;
    private final JwtService jwtService;
    private final AllUserRepository allUserRepository;

    @Autowired
    public LobbyController(LobbyManager lobbyManager, JwtService jwtService, AllUserRepository allUserRepository) {
        this.lobbyManager = lobbyManager;
        this.jwtService = jwtService;
        this.allUserRepository = allUserRepository;
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

    @GetMapping("/{lobbyCode}")
    public ResponseEntity<Map<String, Object>> joinLobby(@PathVariable String lobbyCode, @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> res = new HashMap<>();

        // Extract JWT token
        if (!authHeader.startsWith("Bearer ")) {
            res.put("error", "Invalid authorization header");
            return ResponseEntity.badRequest().body(res);
        }
        String token = authHeader.substring(7);

        // Extract user email from JWT
        String userEmail;
        try {
            userEmail = jwtService.extractUsername(token);
        } catch (Exception e) {
            res.put("error", "Invalid token");
            return ResponseEntity.badRequest().body(res);
        }

        // Fetch user from repository
        Optional<User> userOpt = allUserRepository.findByUsername(userEmail);
        if (userOpt.isEmpty()) {
            res.put("error", "User not found");
            return ResponseEntity.badRequest().body(res);
        }
        User user = userOpt.get();

        // Create Player from User
        Player player = new Player();
        player.setId(user.getId());
        player.setUsername(user.getUsername());
        player.setAccount(user.getRole() == Role.PLAYER);

        if (lobbyManager.joinLobby(lobbyCode, player, res)){
            return ResponseEntity.ok(res);
        }else{
            res.put("error", "Lobby with code " + lobbyCode + " not found.");
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/{lobbyCode}/events")
    public SseEmitter lobbyEvents(@PathVariable String lobbyCode) {
        return lobbyManager.subscribeToLobby(lobbyCode);
    }

}