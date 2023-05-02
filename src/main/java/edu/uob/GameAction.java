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
    
    public void addSubject(final String s) {
        subjects.add(s);
    }
    
    public void addConsumed(final String s) {
        consumed.add(s);
    }
    
    public void addProduced(final String s) {
        produced.add(s);
    }
    
    public void setNarration(final String s) {
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
    
    public boolean isDoable(final String[] words, final Player p, final Location l) {
        boolean subjectSaid = (subjects.isEmpty());
        for (final String sub : subjects) {
            for (final String word : words) {
                if (sub.equals(word)) {
                    subjectSaid = true;
                    break;
                }
            }
        }
        
        return (subjectSaid && arePresent(subjects, p, l) && arePresent(consumed, p, l));
    }
    
    private boolean arePresent(final ArrayList<String> entities, final Player p,
                               final Location l) {
        for (final String ent : entities) {
            if (!p.itemHeld(ent) && !l.artefactIsPresent(ent) &&
                !l.characterIsPresent(ent) && !l.furnitureIsPresent(ent) &&
                !l.pathToLocationExists(ent) && !"health".equals(ent)) {
                return false;
            }
        }
        return true;
    }
}
