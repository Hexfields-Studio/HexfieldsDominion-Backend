package de.hexfieldsstudio.hexfieldsdominion.lobby;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;

@RestController
@RequestMapping(path = "/lobbies", produces = MediaType.APPLICATION_JSON_VALUE)
public class LobbyController {

    @GetMapping
    public void createLobby(@RequestBody CreateLobbyDTO configs) {
        
    }

    @GetMapping("/{code}")
    public void joinLobby(@PathVariable String code) {

    }

}