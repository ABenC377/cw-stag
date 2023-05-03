package edu.uob;

import java.util.ArrayList;
import java.util.List;

/**
 * object representing a game-specific action
 */
public class GameAction {
    /**
     * list of the names of the entities that are requires for the action
     */
    private final List<String> subjects;
    /**
     * list of the names of the entities consumed by the action
     */
    private final List<String> consumed;
    /**
     * list of the names of the entities produced by the action
     */
    private final List<String> produced;
    /**
     * the narration that is given on completion of the action
     */
    private String narration;
    
    /**
     * produces an action object with empty entity lists
     */
    public GameAction() {
        this.subjects = new ArrayList<>();
        this.consumed = new ArrayList<>();
        this.produced = new ArrayList<>();
    }
    
    /**
     * adds name to subject list
     * @param entityName name of the entity
     */
    public void addSubject(final String entityName) {
        subjects.add(entityName);
    }
    
    /**
     * adds name to consumed list
     * @param entityName name of the entity
     */
    public void addConsumed(final String entityName) {
        consumed.add(entityName);
    }
    
    /**
     * adds name to produced list
     * @param entityName name of the entity
     */
    public void addProduced(final String entityName) {
        produced.add(entityName);
    }
    
    /**
     * adds a narration
     * @param narration the narration on completion of action
     */
    public void setNarration(final String narration) {
        this.narration = narration;
    }
    
    /**
     * @return list of names of entities that are consumed
     */
    public List<String> getConsumed() {
        return consumed;
    }
    
    /**
     * @return list of names of entities that are produced
     */
    public List<String> getProduced() {
        return produced;
    }
    
    /**
     * @return narration for this action
     */
    public String getNarration() {
        return narration;
    }
    
    /**
     * determines if the action is currently performable
     * @param words array of words provided by the user
     * @param player the player object
     * @param location the object for the location the player is currently at
     * @param entities list of entities in the game
     * @return whether the action can be performed
     */
    public boolean isDoable(final String[] words,
                            final Player player,
                            final Location location,
                            final List<GameEntity> entities) {
        boolean subjectSaid = subjects.isEmpty();
        for (final String sub : subjects) {
            for (final String word : words) {
                if (sub.equals(word)) {
                    subjectSaid = true;
                    break;
                }
            }
        }

        return noExtraEntities(words, entities) && subjectSaid &&
            arePresent(subjects, player, location);
    }
    
    /**
     * checks whether there are entities beyond the subjects in the user's input
     * @param words the words input by the user
     * @param entities the entities in the game
     * @return whether there are extraneous entity names
     */
    private boolean noExtraEntities(final String[] words,
                                 final List<GameEntity> entities) {
        for (final String word : words) {
            for (final GameEntity entity : entities) {
                if (entity.getName().equals(word) &&
                    !subjects.contains(word)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * confirms whether a list of entities are present in the current locale
     * @param entities the names of the needed entities
     * @param player the player object
     * @param location the location object
     * @return yes/no are the entities here?
     */
    private boolean arePresent(final List<String> entities,
                               final Player player,
                               final Location location) {
        for (final String ent : entities) {
            if (!player.itemHeld(ent) && !location.artefactIsPresent(ent) &&
                !location.characterIsPresent(ent) && !location.furnitureIsPresent(ent) &&
                !location.getName().equals(ent) && !"health".equals(ent)) {
                return false;
            }
        }
        return true;
    }
}
