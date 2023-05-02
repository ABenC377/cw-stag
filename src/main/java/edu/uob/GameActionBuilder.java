package edu.uob;

/**
 * A factory for game action objects
 */
public class GameActionBuilder {
    /**
     * constructor - empty at the moment, nothing needed
     */
    public GameActionBuilder() {}
    
    /**
     * @return a fresh action object
     */
    public GameAction createGameAction() {
        return new GameAction();
    }
}