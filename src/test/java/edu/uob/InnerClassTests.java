package edu.uob;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InnerClassTests {
    @Test
    void testPlayerGetItem() {
        Player player = new Player("Alex");
        Artefact output = player.getItem("axe");
        assertNull(output);
    }
    
    @Test
    void testLocationRemoveFurniture() {
        Location location = new Location("test", "a test space");
        Furniture output = location.removeFurniture("test");
        assertNull(output);
    }
    
    @Test
    void testLocationRemoveArtefact() {
        Location location = new Location("test", "a test space");
        Artefact output = location.removeArtefact("test");
        assertNull(output);
    }
    
    @Test
    void testLocationRemoveCharacter() {
        Location location = new Location("test", "a test space");
        location.removePath("test");
        GameCharacter output = location.removeCharacter("test");
        assertNull(output);
    }
    
    @Test
    void testProduceWithEmptyLocationList() {
        List<Location> locations = new ArrayList<>();
        Location testLocation = new Location("test", "test description");
        testLocation.produce("test", locations);
        assertNotNull(testLocation);
    }
}
