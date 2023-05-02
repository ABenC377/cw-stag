package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

public class ActionHandler {
    private final ArrayList<Location> locations;
    private final Location startLocation;
    private final Location storeRoom;
    
    public ActionHandler(ArrayList<Location> locations,
                         Location startLocation,
                         Location storeRoom) {
        this.locations = locations;
        this.startLocation = startLocation;
        this.storeRoom = storeRoom;
    }
    
    public String handle(GameAction action,
                         Player player,
                         Location location) throws IOException {
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
        
        return (player.checkForDeath(location, startLocation)) ?
            (action.getNarration() +
                "\nYou pass out from the damage\n" +
                "You wake up in " +
                startLocation.getDescription() +
                " without any of your possessions\n") :
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
