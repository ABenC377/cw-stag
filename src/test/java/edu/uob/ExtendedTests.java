package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedTests {
    private GameServer server;
    
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended" +
            "-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended" +
            "-actions-ABC.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }
    
    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will time out if
        // it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> server.handleCommand(command),
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
        sendCommandToServer("test: goto forest");
        String response2 = sendCommandToServer("test: goto forest");
        assertTrue(response2.contains("ERROR"));
    }
    
    @Test
    void test1Step3() {
        sendCommandToServer("test: goto forest");
        String response3 = sendCommandToServer("test: get key");
        assertTrue(response3.contains("picked up"));
    }
    
    @Test
    void test1Step4() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        String response4 = sendCommandToServer("test: goto cabin");
        assertTrue(response4.contains("cabin"));
    }
    
    @Test
    void test1Step5() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open key");
        assertTrue(response5.contains("unlock"));
    }
    
    @Test
    void test1Step6() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        String response6 = sendCommandToServer("test: goto cellar");
        assertTrue(response6.contains("elf"));
    }
    
    @Test
    void test1Step7() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        String response7 = sendCommandToServer("test: health");
        assertTrue(response7.contains("3"));
    }
    
    @Test
    void test1Step8() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        String response8 = sendCommandToServer("test: hit elf");
        assertTrue(response8.contains("lose some health"));
    }
    
    @Test
    void test1Step9() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        sendCommandToServer("test: hit elf");
        String response9 = sendCommandToServer("test: health");
        assertTrue(response9.contains("2"));
    }
    
    @Test
    void test1Step10() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        sendCommandToServer("test: hit elf");
        String response10 = sendCommandToServer("test: hit elf");
        assertTrue(response10.contains("lose some health"));
    }
    
    @Test
    void test1Step11() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        sendCommandToServer("test: hit elf");
        sendCommandToServer("test: hit elf");
        String response11 = sendCommandToServer("test: health");
        assertTrue(response11.contains("1"));
    }
    
    @Test
    void test1Step12() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        sendCommandToServer("test: hit elf");
        sendCommandToServer("test: hit elf");
        String response12 = sendCommandToServer("test: hit elf");
        assertTrue(response12.contains("pass out"));
    }
    
    @Test
    void test1Step13() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        sendCommandToServer("test: hit elf");
        sendCommandToServer("test: hit elf");
        sendCommandToServer("test: hit elf");
        String response13 = sendCommandToServer("test: health");
        assertTrue(response13.contains("3"));
    }
    
    @Test
    void test1Step14() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        sendCommandToServer("test: open key");
        sendCommandToServer("test: goto cellar");
        sendCommandToServer("test: hit elf");
        sendCommandToServer("test: hit elf");
        sendCommandToServer("test: hit elf");
        String response14 = sendCommandToServer("test: inv");
        assertTrue(response14.contains("You are not currently holding any " +
            "items"));
    }
    
    @Test
    void extraEntityTest() {
        sendCommandToServer("test: goto forest");
        sendCommandToServer("test: get key");
        sendCommandToServer("test: goto cabin");
        String response5 = sendCommandToServer("test: open trapdoor with key " +
            "and potion");
        assertTrue(response5.contains("ERROR"));
    }
    
    @Test
    public void testHealing() {
        sendCommandToServer("Alex: get potion");
        sendCommandToServer("Alex: goto forest");
        sendCommandToServer("Alex: get key");
        sendCommandToServer("Alex: goto cabin");
        sendCommandToServer("Alex: open key");
        sendCommandToServer("Alex: goto cellar");
        sendCommandToServer("Alex: hit elf");
        sendCommandToServer("Alex: hit elf");
        sendCommandToServer("Alex: drink potion");
        String response10 = sendCommandToServer("Alex: health");
        assertEquals("Alex's health is at 2", response10);
    }
    
    @Test
    public void testDeath() {
        sendCommandToServer("Alex: get axe");
        sendCommandToServer("Alex: get potion");
        sendCommandToServer("Alex: goto forest");
        sendCommandToServer("Alex: get key");
        sendCommandToServer("Alex: goto cabin");
        sendCommandToServer("Alex: open key");
        sendCommandToServer("Alex: goto cellar");
        sendCommandToServer("Alex: hit elf");
        sendCommandToServer("Alex: hit elf");
        sendCommandToServer("Alex: hit elf");
        String response10 = sendCommandToServer("Alex: look");
        assertEquals("""
            You are in A log cabin in the woods You can see:
            coin: A silver coin
            trapdoor: A locked wooden trapdoor in the floor
            You can see from here:
            forest
            cellar
            """, response10);
    }
    
    @Test
    public void testCallingLumberjack() {
        sendCommandToServer("Alex: goto forest");
        sendCommandToServer("Alex: goto riverbank");
        sendCommandToServer("Alex: get horn");
        String response4 = sendCommandToServer("Alex: blow horn");
        assertEquals("You blow the horn and as if by magic, a lumberjack appears !", response4);
    }
    
    @Test
    public void testCuttingTree() {
        sendCommandToServer("Alex: get axe");
        sendCommandToServer("Alex: goto forest");
        String response3 = sendCommandToServer("Alex: cut tree");
        assertEquals("You cut down the tree with the axe", response3);
    }
    
    @Test
    public void testGrowingTree() {
        sendCommandToServer("Alex: get axe");
        sendCommandToServer("Alex: goto forest");
        sendCommandToServer("Alex: cut tree");
        String response4 = sendCommandToServer("Alex: grow seed");
        assertEquals("You grow a tree", response4);
    }
    
    @Test
    public void testBurningBridge() {
        sendCommandToServer("Alex: get axe");
        sendCommandToServer("Alex: goto forest");
        sendCommandToServer("Alex: cut tree");
        sendCommandToServer("Alex: get log");
        sendCommandToServer("Alex: goto riverbank");
        sendCommandToServer("Alex: bridge river");
        String response7 = sendCommandToServer("Alex: burn down route to " +
            "clearing");
        assertEquals("You burn down the bridge", response7);
    }
    
    @Test
    public void testSinging() {
        String response1 = sendCommandToServer("Alex: sing");
        assertEquals("You sing a sweet song", response1);
    }
    
    @Test
    public void testInvalidAction() {
        String response1 = sendCommandToServer("Alex: blow horn");
        assertEquals("ERROR - no valid instruction in that command", response1);
    }
    
    @Test
    public void testDrinkPotion() {
        String response1 = sendCommandToServer("Alex: drink potion");
        assertEquals("You drink the potion and your health improves", response1);
    }
    
    @Test
    public void testAmbiguousMultiWordTriggers() {
        String response1 = sendCommandToServer("Alex: make music");
        assertEquals("ERROR - invalid/ambiguous command\n", response1);
    }
    
    @Test
    public void testAmbiguousSingleWordTriggers() {
        String response1 = sendCommandToServer("Alex: random");
        assertEquals("ERROR - invalid/ambiguous command\n", response1);
    }
    
    @Test
    public void testRemovingOneOfManyCharacters() {
        sendCommandToServer("Ollie: goto forest");
        sendCommandToServer("Ollie: get key");
        sendCommandToServer("Ollie: goto riverbank");
        sendCommandToServer("Ollie: get horn");
        sendCommandToServer("Ollie: goto forest");
        sendCommandToServer("Ollie: goto cabin");
        sendCommandToServer("Ollie: open trapdoor");
        sendCommandToServer("Ollie: goto cellar");
        sendCommandToServer("Ollie: blow horn");
        sendCommandToServer("Ollie: goto cabin");
        String response1 = sendCommandToServer("Ollie: blow horn");
        assertEquals("You blow the horn and as if by magic, a lumberjack appears !", response1);
    }
    
    @Test
    public void testNotAmbiguousSharedtrigger() {
        sendCommandToServer("Kate: goto forest");
        sendCommandToServer("Kate: get key");
        sendCommandToServer("Kate: goto cabin");
        String response1 = sendCommandToServer("Kate: use key");
        assertEquals("You unlock the door and see steps leading down into a cellar", response1);
    }
    
    @Test
    public void testDoubleTrigger() {
        sendCommandToServer("Chris: get axe");
        sendCommandToServer("Chris: goto forest");
        String response1 = sendCommandToServer("Chris: chop cut tree");
        assertEquals("You cut down the tree with the axe", response1);
    }
    
    @Test
    public void testAmbiguousMultiWordTrigger() {
        String response1 = sendCommandToServer("Gus: make music");
        assertEquals("ERROR - invalid/ambiguous command\n", response1);
    }
    
    @Test
    public void testCommandActionCombo() {
        String response1 = sendCommandToServer("Jake: look for potion to " +
            "drink");
        assertEquals("ERROR - look requires no arguments, so the command " +
            "cannot contain any entity names\n", response1);
    }
    
    @Test
    public void testImpossibleMultiWordTrigger() {
        String response1 = sendCommandToServer("Sion: flirt with lumberjack");
        assertEquals("ERROR - no valid instruction in that command", response1);
    }
    
    @Test
    public void testNoSubjectMentioned() {
        String response1 = sendCommandToServer("Cesca: drink");
        assertEquals("ERROR - no valid instruction in that command", response1);
    }
    
    @Test
    public void testConsumedButNotSubject() {
        String response1 = sendCommandToServer("Eamon: pointless");
        assertEquals("You blow the horn and as if by magic, a lumberjack appears !", response1);
    }
}
