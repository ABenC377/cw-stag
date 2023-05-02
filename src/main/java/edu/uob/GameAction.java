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
    
    public void addSubject(final String entityName) {
        subjects.add(entityName);
    }
    
    public void addConsumed(final String entityName) {
        consumed.add(entityName);
    }
    
    public void addProduced(final String entityName) {
        produced.add(entityName);
    }
    
    public void setNarration(final String entityName) {
        this.narration = entityName;
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
    
    public boolean isDoable(final String[] words,
                            final Player player,
                            final Location location,
                            final ArrayList<GameEntity> entities) {
        boolean subjectSaid = (subjects.isEmpty());
        for (final String sub : subjects) {
            for (final String word : words) {
                if (sub.equals(word)) {
                    subjectSaid = true;
                    break;
                }
            }
        }

        
        return (noExtraEntities(words, entities) &&
            subjectSaid && arePresent(subjects, player, location) &&
            arePresent(consumed, player, location));
    }
    
    private boolean noExtraEntities(final String[] words,
                                 final ArrayList<GameEntity> entities) {
        for (String word : words) {
            for (GameEntity entity : entities) {
                if (entity.getName().equals(word) &&
                    !subjects.contains(word)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean arePresent(final ArrayList<String> entities,
                               final Player player,
                               final Location location) {
        for (final String ent : entities) {
            if (!player.itemHeld(ent) && !location.artefactIsPresent(ent) &&
                !location.characterIsPresent(ent) && !location.furnitureIsPresent(ent) &&
                !location.pathToLocationExists(ent) && !"health".equals(ent)) {
                return false;
            }
        }
        return true;
    }
}
