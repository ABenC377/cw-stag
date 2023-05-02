package edu.uob;

/**
 * A class for the Artefact type of entity
 */
public class Artefact extends GameEntity {
    /**
     * Copies all the behaviour of Entity
     * @param name the name of this Artefact
     * @param description the description provided for this Artefact
     */
    public Artefact(final String name, final String description) {
        super(name, description);
    }
}
