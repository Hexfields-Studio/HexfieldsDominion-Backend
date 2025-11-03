package de.hexfieldsstudio.hexfieldsdominion.lobby;

import org.springframework.web.bind.annotation.*;

import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;

@RestController
@RequestMapping(path = "/lobbies")
public class LobbyController {

    private LobbyManager lobbyManager;

    @PatchMapping
    public void createLobby(@RequestBody CreateLobbyDTO configs) {
        
    }

    @GetMapping("/{code}")
    public void joinLobby(@PathVariable String code) {

    }

}