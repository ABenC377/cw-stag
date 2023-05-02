package edu.uob;

import java.util.HashSet;

/**
 * This is a custom data type for storing multi-word triggers and their
 * associated set of actions - can be stored in an iterable for convenient
 * handling
 */
public class ActionTuple {
    /**
     * The trigger phrase that is a cue for these actions
     */
    private final String trigger;
    /**
     * a Set of actions that are triggered by the trigger phrase
     */
    private final HashSet<GameAction> gameActions = new HashSet<>();
    
    /**
     * Creates a tuple object with an empty Set of actions, and the provided
     * trigger phrase
     * @param trigger A string containing the trigger phrase that must be
     *                provided, verbatim, in a command
     */
    public ActionTuple(final String trigger) {
        this.trigger = trigger;
    }
    
    /**
     * Adds an action to the Set in the tuple
     * @param action The action to be added
     */
    public void addAction(final GameAction action) {
        gameActions.add(action);
    }
    
    /**
     * returns the actions in this tuple
     * @return a pointer to the HashSet of Actions
     */
    public HashSet<GameAction> getActions() {
        return gameActions;
    }
    
    /**
     * returns the trigger phrase for the actions in this tuple
     * @return a String of the trigger phrase
     */
    public String getTrigger() {
        return trigger;
    }
}
