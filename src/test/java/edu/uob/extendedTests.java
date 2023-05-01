package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.Duration;

public class extendedTests {
    private GameServer server;
    
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended" +
            "-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended" +
            "-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }
    
    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
            "Server took too long to respond (probably stuck in an infinite loop)");
    }
    
    
    @Test
    void testPath1() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        
    }
    
}
