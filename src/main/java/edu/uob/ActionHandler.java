package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds functionality for dealing with custom actions
 * A CommandHandler object has one ActionHandler object and one
 * BasicCommandHAndler object
 */

public class ActionHandler {
    /**
     * A List of the locations that are present in this game - for iterating
     * through to find entities
     */
    private final List<Location> locations;
    /**
     * The object where new players start the game, or a player returns to
     * when they run out of health
     */
    private final Location startLocation;
    /**
     * The object where entities are sent when they are consumed
     */
    private final Location storeRoom;
    
    /**
     * Creates an object for handling an action Object
     * @param locations The list of locations in the game
     * @param startLocation The start location for the game
     * @param storeRoom The storeroom location for the game
     */
    public ActionHandler(final List<Location> locations,
                         final Location startLocation,
                         final Location storeRoom) {
        this.locations = locations;
        this.startLocation = startLocation;
        this.storeRoom = storeRoom;
    }
    
    /**
     * method called to execute a custom action
     * @param action the action object that is to be executed
     * @param player the player object that represents the client
     * @param location the location of the player in the game
     * @return returns the string that is to be passed to the client
     * @throws IOException self-explanatory
     */
    public String handle(final GameAction action,
                         final Player player,
                         final Location location) throws IOException {
        for (final String name : action.getConsumed()) {
            consumeEntity(name, player, location);
        }
        
        for (final String name : action.getProduced()) {
            if ("health".equals(name)) {
                player.heal();
            } else {
                location.produce(name, locations);
            }
        }
        
        return player.checkForDeath(location, startLocation) ?
            action.getNarration() +
                "\nYou pass out from the damage\n" +
                "You wake up in " +
                startLocation.getDescription() +
                " without any of your possessions\n" :
            action.getNarration();
    }
    
    private void consumeEntity(final String name,
                               final Player player,
                               final Location location) {
        if (player.itemHeld(name)) {
            final Artefact artefact = player.getItem(name);
            player.removeItem(name);
            storeRoom.addArtefact(artefact);
        } else if (location.artefactIsPresent(name)) {
            storeRoom.addArtefact(location.removeArtefact(name));
        } else if (location.furnitureIsPresent(name)) {
            storeRoom.addFurniture(location.removeFurniture(name));
        } else if (location.pathToLocationExists(name)) {
            location.removePath(name);
        } else if ("health".equals(name)) {
            player.takeDamage();
        }
    }
}
