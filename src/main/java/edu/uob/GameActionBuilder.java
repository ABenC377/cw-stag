package edu.uob;

/**
 * A factory for game action objects
 */
public class GameActionBuilder {
    /**
     * creates and empty factory object
     */
    public GameActionBuilder() {}
    
    /**
     * @return a fresh action object
     */
    public GameAction createGameAction() {
        return new GameAction();
    }
}