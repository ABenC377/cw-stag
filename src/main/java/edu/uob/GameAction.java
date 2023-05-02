package edu.uob;

import java.util.ArrayList;

public class GameAction {
    private final ArrayList<String> subjects;
    private final ArrayList<String> consumed;
    private final ArrayList<String> produced;
    private String narration;
    
    public GameAction() {
        this.subjects = new ArrayList<>();
        this.consumed = new ArrayList<>();
        this.produced = new ArrayList<>();
    }
    
    public void addSubject(String s) {
        subjects.add(s);
    }
    
    public void addConsumed(String s) {
        consumed.add(s);
    }
    
    public void addProduced(String s) {
        produced.add(s);
    }
    
    public void setNarration(String s) {
        this.narration = s;
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
    
    public boolean isDoable(String[] words, Player p, Location l) {
        boolean subjectSaid = (subjects.size() == 0);
        for (String sub : subjects) {
            for (String word : words) {
                if (sub.equals(word)) {
                    subjectSaid = true;
                    break;
                }
            }
        }
        
        if (!subjectSaid) {
            return false;
        }
        
        for (String sub : subjects) {
            if (!p.itemHeld(sub) && !l.artefactIsPresent(sub) &&
                !l.characterIsPresent(sub) && !l.furnitureIsPresent(sub) &&
                !l.pathToLocationExists(sub)) {
                return false;
            }
        }
        
        for (String cons : consumed) {
            if (!p.itemHeld(cons) && !l.artefactIsPresent(cons) &&
                !l.characterIsPresent(cons) && !l.furnitureIsPresent(cons) &&
                !l.pathToLocationExists(cons) && !cons.equals("health")) {
                return false;
            }
        }
        
        return true;
    }
}
