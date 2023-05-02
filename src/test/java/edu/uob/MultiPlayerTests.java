package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MultiPlayerTests {
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
    public void testTwoPlayersSimple1() {
        String sionResponse1 = sendCommandToServer("Sion: look");
        assertEquals("You are in A log cabin in the woods You can see:\n" +
            "potion: A bottle of magic potion\n" +
            "axe: A razor sharp axe\n" +
            "coin: A silver coin\n" +
            "trapdoor: A locked wooden trapdoor in the floor\n" +
            "You can see from here:\n" +
            "forest\n", sionResponse1);
    }
    
    @Test
    public void testTwoPlayersSimple2() {
        String sionResponse1 = sendCommandToServer("Sion: look");
        String simonResponse1 = sendCommandToServer("Simon: look");
        assertEquals("You are in A log cabin in the woods You can see:\n" +
            "potion: A bottle of magic potion\n" +
            "axe: A razor sharp axe\n" +
            "coin: A silver coin\n" +
            "trapdoor: A locked wooden trapdoor in the floor\n" +
            "Sion: A player by the name of Sion\n" +
            "You can see from here:\n" +
            "forest\n", simonResponse1);
    }
}
