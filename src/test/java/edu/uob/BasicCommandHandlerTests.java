package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static edu.uob.BasicCommandType.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicCommandHandlerTests {
    private BasicCommandHandler handler;
    
    @BeforeEach
    void setup() {
        final List<GameEntity> entities = new ArrayList<>();
        handler = new BasicCommandHandler(entities);
    }
    
    @Test
    public void invalidCommandTypeTest() {
        String[] words = "a b c".split(" ");
        String output = handler.handle(ERROR, null, null, words);
        assertEquals("ERROR - not a valid basic command type", output);
    }
}
