package de.hexfieldsstudio.hexfieldsdominion.lobby;

import java.util.HashMap;
import java.util.Map;

import de.hexfieldsstudio.hexfieldsdominion.account.AuthUtils;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.HeartbeatDTO;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.LobbyNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.NotOwnerOfLobbyException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/lobbies")
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyManager lobbyManager;

    @PatchMapping(produces = "application/json")
    public ResponseEntity<@NonNull Map<String, String>> createLobby(@RequestBody(required = false) CreateLobbyDTO dto) {
        Map<String, String> res = new HashMap<>();
        try{
            String lobbyCode = lobbyManager.createLobby(
                    (dto != null) ? dto.configs() : new String[0],
                    AuthUtils.getAuthenticatedUser().getUsername()
            );
            res.put("lobbyCode", lobbyCode);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PostMapping("/{lobbyCode}")
    public ResponseEntity<LobbyManager.@NonNull JoinedLobbyResponse> joinLobby(@PathVariable String lobbyCode) throws LobbyNotFoundException {
        User user = AuthUtils.getAuthenticatedUser();

        LobbyManager.JoinedLobbyResponse response = lobbyManager.joinLobby(lobbyCode, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{lobbyCode}/exists")
    public void doesLobbyWithCodeExist(@PathVariable String lobbyCode, HttpServletResponse response) {
        lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @PostMapping("/{lobbyCode}/heartbeat")
    public void heartbeat(@PathVariable String lobbyCode, @RequestBody HeartbeatDTO dto) throws LobbyNotFoundException {
        Lobby lobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);
        lobby.getHeartbeatHandler().resetTimer(dto.playerId());
    }

    @GetMapping("/{lobbyCode}/events")
    public SseEmitter lobbyEvents(@PathVariable String lobbyCode) {
        User user = AuthUtils.getAuthenticatedUser();

        return lobbyManager.subscribe(lobbyCode, user.getUsername());
    }

    @PostMapping("/{lobbyCode}/match")
    public LobbyManager.CreatedMatchResponse match(@PathVariable String lobbyCode) throws LobbyNotFoundException, NotOwnerOfLobbyException {
        Lobby lobby = lobbyManager.findOccupiedLobbyOrThrow(lobbyCode);
        Match match = lobbyManager.createMatchForLobby(lobby, AuthUtils.getAuthenticatedUser());
        return new LobbyManager.CreatedMatchResponse(match);
    }

}