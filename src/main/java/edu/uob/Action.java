package edu.uob;

import java.util.ArrayList;

public class Action {
    private final int id;
    private final ArrayList<String> subjects;
    private final ArrayList<String> consumed;
    private final ArrayList<String> produced;
    private String narration;
    
    public Action(int id, ArrayList<String> s, ArrayList<String> c,
                  ArrayList<String> p, String n) {
        this.id = id;
        this.subjects = s;
        this.consumed = c;
        this.produced = p;
        this.narration = n;
    }
    
    public ArrayList<String> getSubjects() {
        return subjects;
    }
    public ArrayList<String> getConsumed() {
        return consumed;
    }
    public ArrayList<String> getProduced() {
        return produced;
    }
    public String getNarration() {
        return narration;
    }
}
