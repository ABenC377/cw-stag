package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedTests {
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
    void test1Step1() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
    }
    
    @Test
    void test1Step2() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
    }
    
    @Test
    void test1Step3() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
    }
    
    @Test
    void test1Step4() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
    }
    
    @Test
    void test1Step5() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
    }
    
    @Test
    void test1Step6() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
    }
    
    @Test
    void test1Step7() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
    }
    
    @Test
    void test1Step8() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
    }
    
    @Test
    void test1Step9() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        String response8 = sendCommandToServer("test: hit elf");
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
    }
    
    @Test
    void test1Step10() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        String response8 = sendCommandToServer("test: hit elf");
        String response9 = sendCommandToServer("test: health");
        String response10 = sendCommandToServer("test: hit elf");
        assertTrue(response10.contains("lose some health"));
    }
    
    @Test
    void test1Step11() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        String response8 = sendCommandToServer("test: hit elf");
        String response9 = sendCommandToServer("test: health");
        String response10 = sendCommandToServer("test: hit elf");
        String response11 = sendCommandToServer("test: health");
        assertTrue(response11.contains("1"));
    }
    
    @Test
    void test1Step12() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        String response8 = sendCommandToServer("test: hit elf");
        String response9 = sendCommandToServer("test: health");
        String response10 = sendCommandToServer("test: hit elf");
        String response11 = sendCommandToServer("test: health");
        String response12 = sendCommandToServer("test: hit elf");
        assertTrue(response12.contains("pass out"));
    }
    
    @Test
    void test1Step13() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        String response8 = sendCommandToServer("test: hit elf");
        String response9 = sendCommandToServer("test: health");
        String response10 = sendCommandToServer("test: hit elf");
        String response11 = sendCommandToServer("test: health");
        String response12 = sendCommandToServer("test: hit elf");
        String response13 = sendCommandToServer("test: health");
        assertTrue(response13.contains("3"));
    }
    
    @Test
    void test1Step14() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        String response8 = sendCommandToServer("test: hit elf");
        String response9 = sendCommandToServer("test: health");
        String response10 = sendCommandToServer("test: hit elf");
        String response11 = sendCommandToServer("test: health");
        String response12 = sendCommandToServer("test: hit elf");
        String response13 = sendCommandToServer("test: health");
        String response14 = sendCommandToServer("test: inv");
        assertTrue(response14.contains("You are not currently holding any " +
            "items"));
    }
    
    @Test
    void extraEntityTest() {
        String response1 = sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open trapdoor with key " +
            "and potion");
        assertTrue(response5.contains("ERROR"));
    }
    
    @Test
    public void testHealing() {
        String response1 = sendCommandToServer("Alex: get potion");
        String response2 = sendCommandToServer("Alex: goto forest");
        String response3 = sendCommandToServer("Alex: get key");
        String response4 = sendCommandToServer("Alex: goto cabin");
        String response5 = sendCommandToServer("Alex: open key");
        String response6 = sendCommandToServer("Alex: goto cellar");
        String response7 = sendCommandToServer("Alex: hit elf");
        String response8 = sendCommandToServer("Alex: hit elf");
        String response9 = sendCommandToServer("Alex: drink potion");
        String response10 = sendCommandToServer("Alex: health");
        assertEquals("Alex's health is at 2", response10);
    }
    
    @Test
    public void testDeath() {
        String response0 = sendCommandToServer("Alex: get axe");
        String response1 = sendCommandToServer("Alex: get potion");
        String response2 = sendCommandToServer("Alex: goto forest");
        String response3 = sendCommandToServer("Alex: get key");
        String response4 = sendCommandToServer("Alex: goto cabin");
        String response5 = sendCommandToServer("Alex: open key");
        String response6 = sendCommandToServer("Alex: goto cellar");
        String response7 = sendCommandToServer("Alex: hit elf");
        String response8 = sendCommandToServer("Alex: hit elf");
        String response9 = sendCommandToServer("Alex: hit elf");
        String response10 = sendCommandToServer("Alex: look");
        assertEquals("You are in A log cabin in the woods You can see:\n" +
            "coin: A silver coin\n" +
            "trapdoor: A locked wooden trapdoor in the floor\n" +
            "You can see from here:\n" +
            "forest\n" +
            "cellar\n", response10);
    }
}
