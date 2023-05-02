package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class builtInCommandTests {
    private GameServer server;
    
    @BeforeEach
    void setup() throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "extended" +
            "-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended" +
            "-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }
    
    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
            "Server took too long to respond (probably stuck in an infinite loop)");
    }
    
    @Test
    public void validInvTest1() {
        String response1 = sendCommandToServer("Simon: inv");
        assertEquals("You are not currently holding any items\n", response1);
    }
    
    @Test
    public void validInvTest2() {
        String response1 = sendCommandToServer("Simon: get potion");
        String response2 = sendCommandToServer("Simon: inv");
        assertEquals("You are currently holding:\n" +
            "potion - A bottle of magic potion\n", response2);
    }
    
    @Test
    public void validInvTest3() {
        String response1 = sendCommandToServer("Simon: get potion");
        String response2 = sendCommandToServer("Simon: get axe");
        String response3 = sendCommandToServer("Simon: inv");
        assertEquals("You are currently holding:\n" +
            "potion - A bottle of magic potion\n" +
            "axe - A razor sharp axe\n", response3);
    }
    
    @Test
    public void validGetTest1() {
        String response1 = sendCommandToServer("Simon: get axe");
        assertEquals("Simon picked up axe\n", response1);
    }
    
    @Test
    public void validGetTest2() {
        String response1 = sendCommandToServer("Simon: get axe");
        String response2 = sendCommandToServer("Sion: get coin");
        assertEquals("Sion picked up coin\n", response2);
    }
}
