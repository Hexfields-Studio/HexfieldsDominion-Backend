package de.hexfieldsstudio.hexfieldsdominion.game.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerHueFactoryTest {

    // different tests for short and long because it didn't work for short at some point
    @Test
    void testGenerateHueFromHashShortName() {
        String[] usernames = new String[]{"t", "nw"};

        this.runTestGeneratedHuesForUsernames(usernames);
    }

    @Test
    void testGenerateHueFromHashLongName() {
        String[] usernames = new String[]{"test", "asdfghjk"};

        this.runTestGeneratedHuesForUsernames(usernames);
    }

    private void runTestGeneratedHuesForUsernames(String[] usernames) {
        int[] generatedHues = new int[usernames.length];
        for (int i = 0; i < usernames.length; i++) {
            generatedHues[i] = PlayerHueFactory.generateHueFromHash(usernames[i]);
        }

        for (int i = 0; i < generatedHues.length; i++) {
            assertTrue(generatedHues[i] >= 0);
            assertTrue(generatedHues[i] < 360);
            for (int j = 0; j < generatedHues.length; j++) {
                //ignore same value
                if (i == j) {
                    continue;
                }
                assertNotEquals(generatedHues[i], generatedHues[j]);
            }
        }
    }

}
