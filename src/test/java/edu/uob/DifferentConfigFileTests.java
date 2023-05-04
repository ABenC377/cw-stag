package edu.uob;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * test class to make sure that different types of configuration file (both
 * valid and invalid) are correctly handled
 */
class DifferentConfigFileTests {
    /**
     * server object, through which the tests are run
     */
    private GameServer server;
    
    private String sendCommandToServer(final String command) {
        // Try to send a command to the server - this call will time out if
        // it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> server.handleCommand(command),
            "Server took too long to respond (probably stuck in an infinite loop)");
    }
    
    @Test
    void testNoStoreroom1() {
        final File entitiesFile = Paths.get("config" + File.separator + "extended" +
            "-entities-no-storeroom.dot").toAbsolutePath().toFile();
        final File actionsFile = Paths.get("config" + File.separator + "extended" +
            "-actions-ABC.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
        final String response1 = sendCommandToServer("Simon: says look");
        assertEquals("You are in A log cabin in the woods You can see:\n" +
            "potion: A bottle of magic potion\n" +
            "axe: A razor sharp axe\n" +
            "coin: A silver coin\n" +
            "trapdoor: A locked wooden trapdoor in the floor\n" +
            "You can see from here:\n" +
            "forest\n", response1,
            "if entities file does not have a storeroom, one should be made " +
                "and things should be able to be sent to it (i.e., by being " +
                "consumed");
    }
    
    @Test
    void testNoStoreroom2() {
        final File entitiesFile = Paths.get("config" + File.separator + "extended" +
            "-entities-no-storeroom.dot").toAbsolutePath().toFile();
        final File actionsFile = Paths.get("config" + File.separator + "extended" +
            "-actions-ABC.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
        final String response1 = sendCommandToServer("Simon: drink potion");
        assertEquals("You drink the potion and your health improves", response1,
            "if entities file does not have a storeroom, one should be made " +
                "and things should be able to be sent to it (i.e., by being " +
                "consumed");
    }
}
