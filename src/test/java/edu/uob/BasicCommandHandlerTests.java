package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static edu.uob.BasicCommandType.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test class for making sure that built-in commands are handled correctly
 */
class BasicCommandHandlerTests {
    /**
     * handler object that tests are run through
     */
    private BasicCommandHandler handler;
    
    @BeforeEach
    void setup() {
        final List<GameEntity> entities = new ArrayList<>();
        handler = new BasicCommandHandler(entities);
    }
    
    @Test
    void invalidCommandTypeTest() {
        final String[] words = "a b c".split(" ");
        final String output = handler.handle(ERROR, null, null, words);
        assertEquals("ERROR - not a valid basic command type", output,
            "input of 'a b c' should not be valid as there is no command in" +
                " it");
    }
}
