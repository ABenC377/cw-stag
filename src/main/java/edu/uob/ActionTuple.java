package edu.uob;

import java.util.HashSet;

public class ActionTuple {
    private final String trigger;
    private final HashSet<Action> actions = new HashSet<>();
    
    public ActionTuple(String t) {
        trigger = t;
    }
    
    public void addAction(Action a) {
        actions.add(a);
    }
    
    public HashSet<Action> getActions() {
        return actions;
    }
    
    public String getTrigger() {
        return trigger;
    }
}
