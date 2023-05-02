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
        
        return (subjectSaid && arePresent(subjects, p, l) && arePresent(consumed, p, l));
    }
    
    private boolean arePresent(ArrayList<String> entities, Player p,
                               Location l) {
        for (String ent : entities) {
            if (!p.itemHeld(ent) && !l.artefactIsPresent(ent) &&
                !l.characterIsPresent(ent) && !l.furnitureIsPresent(ent) &&
                !l.pathToLocationExists(ent) && !ent.equals("health")) {
                return false;
            }
        }
        return true;
    }
}
