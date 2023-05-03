package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerClassTests {
    private Player player;
    
    @BeforeEach
    void setup() {
        player = new Player("Alex");
    }
    
    @Test
    public void testGetAbsentItem() {
        Artefact output = player.getItem("axe");
        assertNull(output);
    }
}
