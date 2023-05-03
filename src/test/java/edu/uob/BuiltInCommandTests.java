package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class BuiltInCommandTests {
    private GameServer server;
    
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended" +
            "-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended" +
            "-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }
    
    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will time out if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> server.handleCommand(command),
            "Server took too long to respond (probably stuck in an infinite loop)");
    }
    
    @Test
    public void validInvTest1() {
        String response1 = sendCommandToServer("Simon: inv");
        assertEquals("You are not currently holding any items\n", response1);
    }
    
    @Test
    public void validInvTest2() {
        sendCommandToServer("Simon: get potion");
        String response2 = sendCommandToServer("Simon: inv");
        assertEquals("""
            You are currently holding:
            potion - A bottle of magic potion
            """, response2);
    }
    
    @Test
    public void validInvTest3() {
        sendCommandToServer("Simon: get potion");
        sendCommandToServer("Simon: get axe");
        String response3 = sendCommandToServer("Simon: inv");
        assertEquals("""
            You are currently holding:
            potion - A bottle of magic potion
            axe - A razor sharp axe
            """, response3);
    }
    
    @Test
    public void invalidInvTest1() {
        String response1 = sendCommandToServer("Sion: inv inventory");
        assertEquals("ERROR - invalid/ambiguous command\n", response1);
    }
    
    @Test
    public void invalidInvTest2() {
        String response1 = sendCommandToServer("Simon: is potion in my " +
            "inventory?");
        assertEquals("ERROR - cannot use entity name as decoration for inventory command\n", response1);
    }
    
    @Test
    public void validGetTest1() {
        String response1 = sendCommandToServer("Simon: get axe");
        assertEquals("Simon picked up axe\n", response1);
    }
    
    @Test
    public void validGetTest2() {
        sendCommandToServer("Simon: get axe");
        String response2 = sendCommandToServer("Sion: get coin");
        assertEquals("Sion picked up coin\n", response2);
    }
    
    @Test
    public void invalidGetTest1() {
        String response1 = sendCommandToServer("Sion: Get the potion, get it " +
            "quickly");
        assertEquals("ERROR - invalid/ambiguous command\n", response1);
    }
    
    @Test
    public void invalidGetTest2() {
        String response1 = sendCommandToServer("Simon: get the trapdoor");
        assertEquals("ERROR - get command requires only one argument", response1);
    }
    
    @Test
    public void invalidGetTest3() {
        String response1 = sendCommandToServer("Sion: get flute");
        assertEquals("ERROR - cannot get artefact as it is not in this location", response1);
    }
    
    @Test
    public void validDropTest1() {
        sendCommandToServer("Sion: get potion");
        String response2 = sendCommandToServer("Sion: drop potion");
        assertEquals("Sion dropped potion\n", response2);
    }
    
    @Test
    public void validDropTest2() {
        sendCommandToServer("Sion: get potion");
        String response2 = sendCommandToServer("Sion: inv");
        assertEquals("""
            You are currently holding:
            potion - A bottle of magic potion
            """, response2);
        sendCommandToServer("Sion: drop potion");
        String response4 = sendCommandToServer("Sion: inv");
        assertEquals("You are not currently holding any items\n", response4);
    }
    
    @Test
    public void validDropTest3() {
        sendCommandToServer("Sion: get potion");
        sendCommandToServer("Sion: goto forest");
        sendCommandToServer("Sion: drop potion");
        String response4 = sendCommandToServer("Sion: look");
        assertEquals("""
            You are in A deep dark forest You can see:
            key: A rusty old key
            potion: A bottle of magic potion
            tree: A tall pine tree
            You can see from here:
            cabin
            riverbank
            """, response4);
    }
    
    @Test
    public void invalidDropTest1() {
        String response1 = sendCommandToServer("Simon: drop trapdoor");
        assertEquals("ERROR - drop requires one artefact as its argument", response1);
    }
    
    @Test
    public void invalidDropTest2() {
        String response1 = sendCommandToServer("Sion: drop axe");
        assertEquals("ERROR - cannot drop axe as it is not in your " +
            "inventory\n", response1);
    }
    
    @Test
    public void validGotoTest1() {
        String response1 = sendCommandToServer("Alex: goto forest");
        assertEquals("""
            You arrive in A deep dark forest You can see:
            key: A rusty old key
            tree: A tall pine tree
            You can see from here:
            cabin
            riverbank
            """, response1);
    }
    
    @Test
    public void invalidGotoTest1() {
        String response1 = sendCommandToServer("Joe: goto next location");
        assertEquals("ERROR - goto command requires a location name as an argument", response1);
    }
    
    @Test
    public void invalidGotoTest2() {
        String response1 = sendCommandToServer("Sion: goto axe");
        assertEquals("ERROR - goto requires one location as its argument", response1);
    }
    
    @Test
    public void validLookTest1() {
        String response1 = sendCommandToServer("Neill: look around");
        assertEquals("""
            You are in A log cabin in the woods You can see:
            potion: A bottle of magic potion
            axe: A razor sharp axe
            coin: A silver coin
            trapdoor: A locked wooden trapdoor in the floor
            You can see from here:
            forest
            """, response1);
    }
    
    @Test
    public void invalidLookTest1() {
        String response1 = sendCommandToServer("Joe: look look");
        assertEquals("ERROR - invalid/ambiguous command\n", response1);
    }
    
    @Test
    public void invalidLookTest2() {
        String response1 = sendCommandToServer("Neill: look for axe");
        assertEquals("ERROR - look requires no arguments, so the command cannot contain any entity names\n", response1);
    }
    
    @Test
    public void validHealthTest1() {
        String response1 = sendCommandToServer("Simon: health");
        assertEquals("Simon's health is at 3", response1);
    }
    
    @Test
    public void invalidHealthTest1() {
        String response1 = sendCommandToServer("Sion: health after potion");
        assertEquals("ERROR - health requires no arguments, so the command cannot contain any entity names\n", response1);
    }
}
