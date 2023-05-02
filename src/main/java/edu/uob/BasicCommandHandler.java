package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

import static edu.uob.BasicCommandType.*;

public class BasicCommandHandler {
    
    private final ArrayList<GameEntity> entities;
    public BasicCommandHandler(ArrayList<GameEntity> ents) {
        entities = ents;
    }
    
    public String handle(BasicCommandType command,
                         Player player,
                         Location location,
                         String[] words) throws IOException {
        switch (command) {
            case INV -> {
                return handleInv(words, player);
            }
            case GET -> {
                return handleGet(words, player, location);
            }
            case DROP -> {
                return handleDrop(words, player, location);
            }
            case GOTO -> {
                return handleGoto(words, player, location);
            }
            case LOOK -> {
                return handleLook(words, player, location);
            }
            case HEALTH -> {
                return handleHealth(words, player);
            }
            default -> {
                return "ERROR - not a valid basic command type";
            }
        }
    }
    
    private String handleInv(final String[] words, final Player player) {
        boolean invAlreadySeen = false;
        for (final String word : words) {
            if ("inv".equals(word) || "inventory".equals(word)) {
                if (invAlreadySeen) {
                    return "ERROR - invalid command, too many triggers for " +
                        "inventory command\n";
                } else {
                    invAlreadySeen = true;
                }
            }
        }
        
        for (final String word : words) {
            for (final GameEntity entity : entities) {
                if (entity.getName().toLowerCase().equals(word)) {
                    return "ERROR - cannot use entity name as decoration for " +
                        "inventory command\n";
                }
            }
        }
        
        return player.listItems();
    }
    
    private String handleGet(final String[] words,
                             final Player player,
                             final Location location) {
        
        final int getIndex = findIndex(words, "get");
        if (getIndex == -1) {
            return "ERROR - invalid command, too many triggers for " +
                "get command\n";
        }
        
        Artefact gottenArtefact = findSingleArtefact(words, getIndex);
        if ("ERROR".equals(gottenArtefact.getName())) {
            return "ERROR - get command requires only one argument";
        }
        
        if (!location.artefactIsPresent(gottenArtefact)) {
            return "ERROR - " + gottenArtefact.getName() + " is not present " +
                    "in " + location.getName() + "\n";
        }
        
        location.removeArtefact(gottenArtefact);
        player.pickUpItem(gottenArtefact);
        return (player.getName() + " picked up " + gottenArtefact.getName() +
            "\n");
    }
    
    private String handleDrop(final String[] words,
                              final Player player,
                              final Location location) throws IOException {
        final int dropIndex = findIndex(words, "drop");
        if (dropIndex == -1) {
            return "ERROR - invalid command, too many " +
                "triggers for drop command\n";
        }
        
        Artefact droppedArtefact = findSingleArtefact(words, dropIndex);
        if (droppedArtefact.getName().equals("ERROR")) {
            return "ERROR - drop requires one artefact as its " +
                "argument";
        }
        
        if (!player.itemHeld(droppedArtefact)) {
            return ("ERROR - cannot drop " + droppedArtefact.getName() + " as" +
                " it is not in your inventory\n");
        }
        
        player.removeItem(droppedArtefact);
        location.addArtefact(droppedArtefact);
        return (player.getName() + " dropped " + droppedArtefact.getName() +
            "\n");
    }
    
    private String handleGoto(final String[] words,
                              final Player player,
                              final Location location) {
        final int getIndex = findIndex(words, "goto");
        if (getIndex == -1) {
            return "ERROR - invalid command, too many triggers for " +
                "drop command\n";
        }
        
        Location gotoLocation = null;
        for (int j = getIndex; j < words.length; j++) {
            final String word = words[j];
            for (final GameEntity entity : entities) {
                if (word.equals(entity.getName().toLowerCase())) {
                    if (entity instanceof Location && gotoLocation == null) {
                        gotoLocation = (Location)entity;
                    } else {
                        return "ERROR - goto requires one location as its " +
                            "argument";
                    }
                }
            }
        }
        
        if (gotoLocation == null) {
            return "ERROR - goto command requires a location name as an " +
                "argument";
        }
        
        return gotoLocation(player, location, gotoLocation);
    }
    
    private String handleLook(final String[] words,
                              final Player player,
                              final Location location) {
        boolean looked = false;
        for (final String word : words) {
            if ("look".equals(word)) {
                if (looked) {
                    return "ERROR - invalid command, too many triggers for " +
                        "look command\n";
                } else {
                    looked = true;
                }
            }
            
            for (final GameEntity entity : entities) {
                if (word.equals(entity.getName().toLowerCase())) {
                    return "ERROR - look requires no arguments, so the " +
                        "command cannot contain any entity names\n";
                }
            }
        }
        
        return location.lookAround(player);
    }
    
    private String handleHealth(final String[] words,
                                final Player player) {
        boolean healthed = false;
        for (final String word : words) {
            if ("health".equals(word)) {
                if (healthed) {
                    return "ERROR - invalid command, too many triggers for " +
                        "health command";
                } else {
                    healthed = true;
                }
            }
            
            for (final GameEntity entity : entities) {
                if (word.equals(entity.getName().toLowerCase())) {
                    return "ERROR - health requires no arguments, so the " +
                        "command cannot contain any entity names\n";
                }
            }
        }
        
        return player.reportHealth();
    }
    
    private int findIndex(final String[] words, final String toFind) {
        int output = -1;
        int index = 0;
        for (final String word : words) {
            if (word.equals(toFind) && output == -1) {
                output = index;
            } else if (word.equals(toFind)) {
                return -1;
            }
            index++;
        }
        return output;
    }
    
    private String gotoLocation(final Player player,
                                final Location currentLocation,
                                final Location gotoLocation) {
        if (currentLocation.pathToLocationExists(gotoLocation.getName().toLowerCase())) {
            gotoLocation.addCharacter(player);
            currentLocation.removeCharacter(player);
            return gotoLocation.getArrivalString(player);
        }
        return ("ERROR - " + player.getName() + " could not go to " +
            gotoLocation.getName() + " as no valid path exists\n");
    }
    
    private Artefact findSingleArtefact(final String[] words,
                                        final int startIndex) {
        Artefact gottenArtefact = null;
        Artefact err = new Artefact("ERROR", "ERROR");
        
        for (int jndex = startIndex; jndex < words.length; jndex++) {
            final String word = words[jndex];
            for (final GameEntity entity : entities) {
                if (word.equals(entity.getName().toLowerCase()) &&
                    entity instanceof Artefact && gottenArtefact == null) {
                    gottenArtefact = (Artefact)entity;
                } else if (word.equals(entity.getName().toLowerCase())) {
                    gottenArtefact = err;
                }
            }
        }
        
        return gottenArtefact;
    }
}
