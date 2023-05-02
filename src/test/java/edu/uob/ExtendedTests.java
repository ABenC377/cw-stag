package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

class ExtendedTests {
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
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
    }
    
    @Test
    void test1Step3() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
    }
    
    @Test
    void test1Step4() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
    }
    
    @Test
    void test1Step5() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
    }
    
    @Test
    void test1Step6() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
    }
    
    @Test
    void test1Step7() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
    }
    
    @Test
    void test1Step8() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
    }
    
    @Test
    void test1Step9() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
    }
    
    @Test
    void test1Step10() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
        String response10 = sendCommandToServer("test: hit elf");
        assertTrue(response10.contains("lose some health"));
    }
    
    @Test
    void test1Step11() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
        String response10 = sendCommandToServer("test: hit elf");
        assertTrue(response10.contains("lose some health"));
        String response11 = sendCommandToServer("test: health");
        assertTrue(response11.contains("1"));
    }
    
    @Test
    void test1Step12() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
        String response10 = sendCommandToServer("test: hit elf");
        assertTrue(response10.contains("lose some health"));
        String response11 = sendCommandToServer("test: health");
        assertTrue(response11.contains("1"));
        String response12 = sendCommandToServer("test: hit elf");
        assertTrue(response12.contains("pass out"));
    }
    
    @Test
    void test1Step13() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
        String response10 = sendCommandToServer("test: hit elf");
        assertTrue(response10.contains("lose some health"));
        String response11 = sendCommandToServer("test: health");
        assertTrue(response11.contains("1"));
        String response12 = sendCommandToServer("test: hit elf");
        assertTrue(response12.contains("pass out"));
        String response13 = sendCommandToServer("test: health");
        assertTrue(response13.contains("3"));
    }
    
    @Test
    void test1Step14() {
        String response1 = sendCommandToServer("test: goto forest");
        assertTrue(response1.contains("key"));
        assertTrue(response1.contains("cabin"));
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
        String response10 = sendCommandToServer("test: hit elf");
        assertTrue(response10.contains("lose some health"));
        String response11 = sendCommandToServer("test: health");
        assertTrue(response11.contains("1"));
        String response12 = sendCommandToServer("test: hit elf");
        assertTrue(response12.contains("pass out"));
        String response13 = sendCommandToServer("test: health");
        assertTrue(response13.contains("3"));
        String response14 = sendCommandToServer("test: inv");
        assertTrue(response14.contains("You are not currently holding any " +
            "items"));
    }
}
