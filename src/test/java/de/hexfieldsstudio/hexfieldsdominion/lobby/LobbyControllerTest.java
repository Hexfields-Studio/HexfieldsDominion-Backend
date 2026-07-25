package de.hexfieldsstudio.hexfieldsdominion.lobby;

import de.hexfieldsstudio.hexfieldsdominion.account.AuthUtils;
import de.hexfieldsstudio.hexfieldsdominion.account.user.Role;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import de.hexfieldsstudio.hexfieldsdominion.game.Match;
import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.CreateLobbyDTO;
import de.hexfieldsstudio.hexfieldsdominion.lobby.dto.HeartbeatDTO;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.LobbyNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.heartbeat.HeartbeatHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LobbyControllerTest {

    private static final String LOBBY_CODE = "ABC123D";

    @InjectMocks
    private LobbyController lobbyController;

    @Mock
    private LobbyManager lobbyManager;

    @Test
    void testCreateLobbyWithDto() throws Exception {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        CreateLobbyDTO dto = new CreateLobbyDTO(new String[2]);

        when(lobbyManager.createLobby(dto.configs(), user.getUsername())).thenReturn(LOBBY_CODE);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            ResponseEntity<Map<String, String>> responseEntity = lobbyController.createLobby(dto);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            Map<String, String> response = responseEntity.getBody();
            assertNotNull(response);
            assertEquals(1, response.size());
            assertTrue(response.containsKey("lobbyCode"));
            assertEquals(LOBBY_CODE, response.get("lobbyCode"));
        }
    }

    @Test
    void testCreateLobbyWithoutDto() throws Exception {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        when(lobbyManager.createLobby(new String[0], user.getUsername())).thenReturn(LOBBY_CODE);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            ResponseEntity<Map<String, String>> responseEntity = lobbyController.createLobby(null);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            Map<String, String> response = responseEntity.getBody();
            assertNotNull(response);
            assertEquals(1, response.size());
            assertTrue(response.containsKey("lobbyCode"));
            assertEquals(LOBBY_CODE, response.get("lobbyCode"));
        }
    }

    @Test
    void testCreateLobbyException() throws Exception {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        String exceptionMessage = "some msg";

        when(lobbyManager.createLobby(new String[0], user.getUsername())).thenThrow(new Exception(exceptionMessage));

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            ResponseEntity<Map<String, String>> responseEntity = lobbyController.createLobby(null);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
            Map<String, String> response = responseEntity.getBody();
            assertNotNull(response);
            assertEquals(1, response.size());
            assertTrue(response.containsKey("error"));
            assertEquals(exceptionMessage, response.get("error"));
        }
    }

    @Test
    void testJoinLobbyLobbyExists() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        LobbyManager.JoinedLobbyResponse expectedResponse = mock(LobbyManager.JoinedLobbyResponse.class);

        when(lobbyManager.joinLobby(LOBBY_CODE, user)).thenReturn(expectedResponse);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            ResponseEntity<LobbyManager.JoinedLobbyResponse> responseEntity = lobbyController.joinLobby(LOBBY_CODE);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(expectedResponse, responseEntity.getBody());
        }
    }

    @Test
    void testJoinLobbyLobbyDoesNotExist() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        when(lobbyManager.joinLobby(LOBBY_CODE, user)).thenThrow(new LobbyNotFoundException(LOBBY_CODE));

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            assertThrows(LobbyNotFoundException.class, () -> lobbyController.joinLobby(LOBBY_CODE));
        }
    }

    @Test
    void testDoesLobbyWithCodeExistTrue() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        Lobby existingLobby = mock(Lobby.class);

        when(lobbyManager.findOccupiedLobbyOrThrow(LOBBY_CODE)).thenReturn(existingLobby);

        lobbyController.doesLobbyWithCodeExist(LOBBY_CODE, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(response, times(1)).setStatus(anyInt());
    }

    @Test
    void testDoesLobbyWithCodeExistFalse() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(lobbyManager.findOccupiedLobbyOrThrow(LOBBY_CODE)).thenThrow(new LobbyNotFoundException(LOBBY_CODE));

        assertThrows(LobbyNotFoundException.class, () -> lobbyController.doesLobbyWithCodeExist(LOBBY_CODE, response));
    }

    @Test
    void testHeartbeatLobbyExists() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        Lobby existingLobby = mock(Lobby.class);
        HeartbeatHandler heartbeatHandler = mock(HeartbeatHandler.class);
        when(existingLobby.getHeartbeatHandler()).thenReturn(heartbeatHandler);

        HeartbeatDTO dto = new HeartbeatDTO(0);

        when(lobbyManager.findOccupiedLobbyOrThrow(LOBBY_CODE)).thenReturn(existingLobby);

        lobbyController.heartbeat(LOBBY_CODE, dto, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(response, times(1)).setStatus(anyInt());
    }

    @Test
    void testHeartbeatLobbyDoesNotExist() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        HeartbeatDTO dto = new HeartbeatDTO(0);

        when(lobbyManager.findOccupiedLobbyOrThrow(LOBBY_CODE)).thenThrow(new LobbyNotFoundException(LOBBY_CODE));

        assertThrows(LobbyNotFoundException.class, () -> lobbyController.heartbeat(LOBBY_CODE, dto, response));
    }

    @Test
    void testLobbyEvents() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        SseEmitter expectedEmitter = mock(SseEmitter.class);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);
            when(lobbyManager.subscribe(LOBBY_CODE, user.getUsername())).thenReturn(expectedEmitter);

            SseEmitter createdEmitter = lobbyController.lobbyEvents(LOBBY_CODE);

            assertEquals(expectedEmitter, createdEmitter);
        }
    }

    @Test
    void testMatchLobbyExists() {
        User user = User.builder()
                .username("testuser")
                .role(Role.GUEST)
                .build();

        Lobby existingLobby = mock(Lobby.class);
        Match match = mock(Match.class);
        when(match.getUuid()).thenReturn(UUID.randomUUID());

        when(lobbyManager.findOccupiedLobbyOrThrow(LOBBY_CODE)).thenReturn(existingLobby);
        when(lobbyManager.createMatchForLobby(existingLobby, user, LobbyController.BOARD_RADIUS)).thenReturn(match);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getAuthenticatedUser).thenReturn(user);

            LobbyManager.CreatedMatchResponse response = lobbyController.match(LOBBY_CODE);

            assertEquals(match.getUuid().toString(), response.matchUUID());
        }
    }

    @Test
    void testMatchLobbyDoesNotExist() {
        when(lobbyManager.findOccupiedLobbyOrThrow(LOBBY_CODE)).thenThrow(new LobbyNotFoundException(LOBBY_CODE));

        assertThrows(LobbyNotFoundException.class, () -> lobbyController.match(LOBBY_CODE));
    }

}
