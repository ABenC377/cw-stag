package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class CommandFormatTests {
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
        // Try to send a command to the server - this call will time out if
        // it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> server.handleCommand(command),
            "Server took too long to respond (probably stuck in an infinite loop)");
    }
    
    @Test
    void lowerCaseTest() {
        String response1 = sendCommandToServer("Neill: look");
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
    void upperCaseTest() {
        String response1 = sendCommandToServer("Neill: LOOK");
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
    void mixedCaseTest() {
        String response1 = sendCommandToServer("Neill: LooK");
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
    void decoratedTest1() {
        String response1 = sendCommandToServer("Neill: Look around");
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
    void decoratedTest2() {
        String response1 = sendCommandToServer("Neill: go and get that axe");
        assertEquals("Neill picked up axe\n", response1);
    }
    
    @Test
    void decoratedTest3() {
        sendCommandToServer("Sion: go and get that axe");
        String response2 = sendCommandToServer("Sion: goto the forest");
        assertEquals("""
            You arrive in A deep dark forest You can see:
            key: A rusty old key
            tree: A tall pine tree
            You can see from here:
            cabin
            riverbank
            """, response2);
    }
    
    @Test
    void decoratedTest4() {
        sendCommandToServer("Sion: go and get that axe");
        sendCommandToServer("Sion: goto the forest");
        String response3 = sendCommandToServer("Sion: with that axe, cut down" +
            " the tree");
        assertEquals("You cut down the tree with the axe", response3);
    }
    
    @Test
    void punctuationTest1() {
        String response1 = sendCommandToServer("Neill: Look here, there, and " +
            "everywhere");
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
    void tooManyColons() {
        String response1 = sendCommandToServer("Alex: goto: forest");
        assertEquals("You arrive in A deep dark forest You can see:\n" +
            "key: A rusty old key\n" +
            "tree: A tall pine tree\n" +
            "You can see from here:\n" +
            "cabin\n" +
            "riverbank\n", response1);
    }
    
    @Test
    void invalidUsername() {
        String response1 = sendCommandToServer("Al_ex: look");
        assertEquals("ERROR: invalid username", response1);
    }
}
