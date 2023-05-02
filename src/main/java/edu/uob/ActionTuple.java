package edu.uob;

import java.util.HashSet;

public class ActionTuple {
    private final String trigger;
    private final HashSet<GameAction> gameActions = new HashSet<>();
    
    public ActionTuple(final String t) {
        trigger = t;
    }
    
    public void addAction(final GameAction a) {
        gameActions.add(a);
    }
    
    public HashSet<GameAction> getActions() {
        return gameActions;
    }
    
    public String getTrigger() {
        return trigger;
    }
}
