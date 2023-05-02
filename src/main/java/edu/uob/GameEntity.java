package edu.uob;

/**
 * parent class for all the different types of entity in the game
 */
public class GameEntity
{
    /**
     * the entities name, used to refer to it by the user
     */
    private final String name;
    /**
     * a description of the entity, for giving the user more detail about it
     */
    private final String description;
    
    /**
     * produces an entity object
     * @param name the entity name
     * @param description a description of the entity
     */
    public GameEntity(final String name, final String description)
    {
        this.name = name;
        this.description = description;
    }
    
    /**
     * @return the entity's name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @return the entity's description
     */
    public String getDescription()
    {
        return description;
    }
}
