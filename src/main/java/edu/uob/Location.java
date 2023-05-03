package edu.uob;

import java.util.ArrayList;
import java.util.List;

/**
 * a location in the game
 */
public class Location extends GameEntity {
    /**
     * list of the furniture that are present in the location
     */
    private final List<Furniture> furniture;
    /**
     * a list of the artefacts that are present in the location
     */
    private final List<Artefact> artefacts;
    /**
     * a list of the characters that are present in the location
     */
    private final List<GameCharacter> characters;
    /**
     * a list of the locations that are directly accessible from the location
     */
    private final List<Location> paths;
    
    /**
     * produces a location object with empty entity lists
     * @param name the name of the location
     * @param description a description of the location
     */
    public Location(final String name, final String description) {
        super(name, description);
        furniture = new ArrayList<>();
        artefacts = new ArrayList<>();
        characters = new ArrayList<>();
        paths = new ArrayList<>();
    }
    
    /**
     * add a furniture object to the list of furniture in this location
     * @param furniture the furniture object
     */
    public void addFurniture(final Furniture furniture) {
        this.furniture.add(furniture);
    }
    
    /**
     * add an artefact object to the list of artefacts in this location
     * @param artefact the artefact object
     */
    public void addArtefact(final Artefact artefact) {
        artefacts.add(artefact);
    }
    
    /**
     * add a character object to the list of characters in this location
     * @param character the character object
     */
    public void addCharacter(final GameCharacter character) {
        characters.add(character);
    }
    
    /**
     * add a location to the list of accessible locations
     * @param location the location object
     */
    public void addPath(final Location location) {
        paths.add(location);
    }
    
    /**
     * checks whether a piece of furniture is present in this location
     * @param name the name of the furniture
     * @return yes/no
     */
    public boolean furnitureIsPresent(final String name) {
        for (final Furniture furn : furniture) {
            if (furn.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * checks whether an artefact is present in this location
     * @param name the name of the artefact
     * @return yes/no
     */
    public boolean artefactIsPresent(final String name) {
        for (final Artefact artefact : artefacts) {
            if (artefact.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * checks whether a character is present in this location
     * @param name the name of the character
     * @return yes/no
     */
    public boolean characterIsPresent(final String name) {
        for (final GameCharacter character : characters) {
            if (character.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * checks whether there is a path to the given location from here
     * @param name the name of the location
     * @return yes/no
     */
    public boolean pathToLocationExists(final String name) {
        for (final Location location : paths) {
            if (location.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * removes a furniture object from the list of furniture at this location
     * @param name the name of the furniture to be removed
     * @return the furniture object being removed, or null if there is no
     * furniture of the given name
     */
    public Furniture removeFurniture(final String name) {
        Furniture output = null;
        for (final Furniture furn : furniture) {
            if (furn.getName().equals(name)) {
                output = furn;
            }
        }
        if (output != null) {
            furniture.remove(output);
        }
        return output;
    }
    
    /**
     * removes an artefact object from the list of artefacts
     * @param artefact the artefact object
     */
    public void removeArtefact(final Artefact artefact) {
        artefacts.remove(artefact);
    }
    
    /**
     * removes an artefact object from the list of artefacts at this location
     * @param name the name of the  artefact to be removed
     * @return the artefact object being removed, or null if there is no
     * artefact of the given name
     */
    public Artefact removeArtefact(final String name) {
        Artefact output = null;
        for (final Artefact artefact : artefacts) {
            if (artefact.getName().equals(name)) {
                output = artefact;
            }
        }
        if (output != null) {
            artefacts.remove(output);
        }
        return output;
    }
    
    /**
     * removes a character object from the character list
     * @param character the character object to be removed
     */
    public void removeCharacter(final GameCharacter character) {
        characters.remove(character);
    }
    
    /**
     * removes a character object from the list of characters at this location
     * @param name the name of the  character to be removed
     * @return the character object being removed, or null if there is no
     * character of the given name
     */
    public GameCharacter removeCharacter(final String name) {
        GameCharacter output = null;
        for (final GameCharacter character : characters) {
            if (character.getName().equals(name)) {
                output = character;
            }
        }
        if (output != null) {
            characters.remove(output);
        }
        return output;
    }
    
    /**
     * removes a location object from the paths list
     * @param name the name of the location to be removed
     */
    public void removePath(final String name) {
        Location toRemove = null;
        for (final Location destination : paths) {
            if (destination.getName().equals(name)) {
                toRemove = destination;
            }
        }
        if (toRemove != null) {
            paths.remove(toRemove);
        }
    }
    
    /**
     * gives the list of characters in this location
     * @return the list of character objects
     */
    public List<GameCharacter> getCharacters() {
        return characters;
    }
    
    /**
     * gives a string for a user to see if their player arrives here
     * @param player the player object
     * @return the string to send to the client
     */
    public String getArrivalString(final Player player) {
        String output = lookAround(player);
        return output.replace("are in", "arrive in");
    }
    
    /**
     * gives the text that a user sees when looking around this location
     * @param player the user's player object
     * @return the string that is to be sent to the client
     */
    public String lookAround(final Player player) {
        final StringBuilder builder = new StringBuilder();
        builder.append("You are in ")
            .append(this.getDescription())
            .append(" You can see:\n");
        for (final Artefact artefact : artefacts) {
            builder.append(artefact.getName())
                .append(": ")
                .append(artefact.getDescription())
                .append(System.lineSeparator());
        }
        for (final Furniture furn : furniture) {
            builder.append(furn.getName())
                .append(": ")
                .append(furn.getDescription())
                .append(System.lineSeparator());
        }
        for (final GameCharacter character : characters) {
            if (!character.equals(player)) {
                builder.append(character.getName())
                    .append(": ")
                    .append(character.getDescription())
                    .append(System.lineSeparator());
            }
        }
        builder.append("You can see from here:\n");
        for (final Location location : paths) {
            builder.append(location.getName());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
    
    /**
     * produces an entity into this location
     * @param entityName the name of the entity to be produced
     * @param locations a list of all the locations in the game
     */
    public void produce(final String entityName,
                        final List<Location> locations) {
        for (final Location l : locations) {
            if (l.getName().equals(entityName)) {
                paths.add(l);
                break;
            } else if (l.characterIsPresent(entityName)) {
                characters.add(l.removeCharacter(entityName));
                break;
            } else if (l.furnitureIsPresent(entityName)) {
                furniture.add(l.removeFurniture(entityName));
                break;
            } else if (l.artefactIsPresent(entityName)) {
                artefacts.add(l.removeArtefact(entityName));
                break;
            }
        }
    }
}
