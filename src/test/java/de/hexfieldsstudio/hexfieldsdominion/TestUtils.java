package de.hexfieldsstudio.hexfieldsdominion;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {

    public static void assertLobbyCodeValid(String lobbyCode) {
        assertNotNull(lobbyCode);
        assertEquals(7, lobbyCode.length());
        assertTrue(lobbyCode.matches("^[A-Z0-9]+$"));
    }

}
