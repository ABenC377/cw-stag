package edu.uob;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * test class for making sure the inner runnings of some of the classes are
 * working as intended
 */
class InnerClassTests {
    @Test
    void testPlayerGetItem() {
        final Player player = new Player("Alex");
        final Artefact output = player.getItem("axe");
        assertNull(output, "geting an item not in inventory should return " +
            "null");
    }
    
    @Test
    void testLocationRemoveFurniture() {
        final Location location = new Location("test", "a test space");
        final Furniture output = location.removeFurniture("test");
        assertNull(output, "removing furniture not at a location should " +
            "return null");
    }
    
    @Test
    void testLocationRemoveArtefact() {
        final Location location = new Location("test", "a test space");
        final Artefact output = location.removeArtefact("test");
        assertNull(output, "removing artefact not at a location should " +
            "return null");
    }
    
    @Test
    void testLocationRemoveCharacter() {
        final Location location = new Location("test", "a test space");
        location.removePath("test");
        final GameCharacter output = location.removeCharacter("test");
        assertNull(output, "removing character not at a location should " +
            "return null");
    }
    
    @Test
    void testProduceWithEmptyLocationList() {
        final List<Location> locations = new ArrayList<>();
        final Location testLocation = new Location("test", "test description");
        testLocation.produce("test", locations);
        assertNotNull(testLocation, "producing an item that is not in the " +
            "locations should return null");
    }
}
