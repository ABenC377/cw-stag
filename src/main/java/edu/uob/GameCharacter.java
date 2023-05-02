package edu.uob;

/**
 * class for characters in the game
 */
public class GameCharacter extends GameEntity {
    /**
     * produces a character object
     * @param name the character's name
     * @param description a description of the character
     */
    public GameCharacter(final String name, final String description) {
        super(name, description);
    }
}
