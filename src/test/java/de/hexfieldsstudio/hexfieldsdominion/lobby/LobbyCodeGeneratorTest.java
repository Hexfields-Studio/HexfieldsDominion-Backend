package de.hexfieldsstudio.hexfieldsdominion.lobby;

import org.junit.jupiter.api.Test;

import static de.hexfieldsstudio.hexfieldsdominion.TestUtils.assertLobbyCodeValid;

public class LobbyCodeGeneratorTest {

    @Test
    void testGenerateCode() {
        String code = LobbyCodeGenerator.generateCode();

        assertLobbyCodeValid(code);
    }

}
