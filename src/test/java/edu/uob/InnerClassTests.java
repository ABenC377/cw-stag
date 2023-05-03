package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InnerClassTests {
    @Test
    public void testPlayerGetItem() {
        Player player = new Player("Alex");
        Artefact output = player.getItem("axe");
        assertNull(output);
    }
    
    @Test
    public void testLocationRemoveFurniture() {
        Location location = new Location("test", "a test space");
        Furniture output = location.removeFurniture("test");
        assertNull(output);
    }
    
    @Test
    public void testLocationRemoveArtefact() {
        Location location = new Location("test", "a test space");
        Artefact output = location.removeArtefact("test");
        assertNull(output);
    }
}
