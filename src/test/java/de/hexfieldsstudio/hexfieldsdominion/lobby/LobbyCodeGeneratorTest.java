package de.hexfieldsstudio.hexfieldsdominion.lobby;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LobbyCodeGeneratorTest {

    @Test
    void testGenerateCode() {
        String code = LobbyCodeGenerator.generateCode();

        assertEquals(7, code.length());
        assertTrue(code.matches("^[A-Z0-9]+$"));
    }

}
