package edu.uob;

import java.util.HashSet;

public class ActionTuple {
    private final String trigger;
    private final HashSet<GameAction> gameActions = new HashSet<>();
    
    public ActionTuple(final String trigger) {
        this.trigger = trigger;
    }
    
    public void addAction(final GameAction action) {
        gameActions.add(action);
    }
    
    public HashSet<GameAction> getActions() {
        return gameActions;
    }
    
    public String getTrigger() {
        return trigger;
    }
}
