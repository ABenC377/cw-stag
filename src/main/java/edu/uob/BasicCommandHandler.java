package edu.uob;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Handler classs for basic commands
 */
public class BasicCommandHandler {
    /**
     * a List of the entities that exist in the game
     */
    private final List<GameEntity> entities;
    
    /**
     * produces a handler object for basic commands, provided with the game
     * metadata needed to handle the basic commands
     * @param ents An arrayList of the entities that exist in the game
     */
    public BasicCommandHandler(final List<GameEntity> ents) {
        entities = ents;
    }
    
    /**
     * @param command the type of command
     * @param player the player object
     * @param location the current location of the player
     * @param words the words of the user's command
     * @return the string that is to be passed back to client
     * @throws IOException self-explanatory
     */
    public String handle(final BasicCommandType command,
                         final Player player,
                         final Location location,
                         final String[] words) throws IOException {
        String output;
        switch (command) {
            case INV -> {
                output = handleInv(words, player);
            }
            case GET -> {
                output = handleGet(words, player, location);
            }
            case DROP -> {
                output = handleDrop(words, player, location);
            }
            case GOTO -> {
                output = handleGoto(words, player, location);
            }
            case LOOK -> {
                output = handleLook(words, player, location);
            }
            case HEALTH -> {
                output = handleHealth(words, player);
            }
            default -> {
                output = "ERROR - not a valid basic command type";
            }
        }
        return output;
    }
    
    /**
     * handles the inventory command
     * @param words the words of the user's input
     * @param player the player object
     * @return the string that is to be passed back to the client
     */
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
                if (entity.getName().toLowerCase(Locale.ENGLISH).equals(word)) {
                    return "ERROR - cannot use entity name as decoration for " +
                        "inventory command\n";
                }
            }
        }
        
        return player.listItems();
    }
    
    /**
     * handles the get command
     * @param words the words of the user's input
     * @param player the player object
     * @param location the players current location object
     * @return the string to be passed back to the client
    */
    private String handleGet(final String[] words,
                             final Player player,
                             final Location location) {
        
        final int getIndex = findIndex(words, "get");
        if (getIndex == -1) {
            return "ERROR - invalid command, too many triggers for " +
                "get command\n";
        }
        
        final Artefact gottenArtefact = findSingleArtefact(words, getIndex);
        if (gottenArtefact == null) {
            return "ERROR - cannot get artefact as it is not in this location";
        }
        
        if ("ERROR".equals(gottenArtefact.getName())) {
            return "ERROR - get command requires only one argument";
        }
        
        if (!location.artefactIsPresent(gottenArtefact)) {
            return "ERROR - " + gottenArtefact.getName() + " is not present " +
                    "in " + location.getName() + "\n";
        }
        
        location.removeArtefact(gottenArtefact);
        player.pickUpItem(gottenArtefact);
        return player.getName() + " picked up " + gottenArtefact.getName() +
            "\n";
    }
    
    /**
     * handles the drop command
     * @param words teh words of the user's input
     * @param player the player object
     * @param location the player's current location object
     * @return the string to be passed back to the client
     * @throws IOException self-explanatory
     */
    private String handleDrop(final String[] words,
                              final Player player,
                              final Location location) throws IOException {
        final int dropIndex = findIndex(words, "drop");
        if (dropIndex == -1) {
            return "ERROR - invalid command, too many " +
                "triggers for drop command\n";
        }
        
        final Artefact droppedArtefact = findSingleArtefact(words, dropIndex);
        if ("ERROR".equals(droppedArtefact.getName())) {
            return "ERROR - drop requires one artefact as its " +
                "argument";
        }
        
        if (!player.itemHeld(droppedArtefact)) {
            return "ERROR - cannot drop " + droppedArtefact.getName() + " as" +
                " it is not in your inventory\n";
        }
        
        player.removeItem(droppedArtefact);
        location.addArtefact(droppedArtefact);
        return player.getName() + " dropped " + droppedArtefact.getName() +
            "\n";
    }
    
    /**
     * handles the goto command
     * @param words the words of the user's input
     * @param player the player object
     * @param location the player's current location object
     * @return the string to be passed back to the client
     */
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
                if (word.equals(entity.getName().toLowerCase(Locale.ENGLISH))) {
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
    
    /**
     * handles the drop command
     * @param words teh words of the user's input
     * @param player the player object
     * @param location the player's current location object
     * @return the string to be passed back to the client
     */
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
                if (word.equals(entity.getName().toLowerCase(Locale.ENGLISH))) {
                    return "ERROR - look requires no arguments, so the " +
                        "command cannot contain any entity names\n";
                }
            }
        }
        
        return location.lookAround(player);
    }
    
    /**
     * handles the drop command
     * @param words teh words of the user's input
     * @param player the player object
     * @return the string to be passed back to the client
     */
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
                if (word.equals(entity.getName().toLowerCase(Locale.ENGLISH))) {
                    return "ERROR - health requires no arguments, so the " +
                        "command cannot contain any entity names\n";
                }
            }
        }
        
        return player.reportHealth();
    }
    
    /**
     * Finds the index of toFind in the words array
     * @param words array of words provided by the user
     * @param toFind the word we want to find
     * @return the index of the word, or -1 if it is not present
     */
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
    
    /**
     * moves a player object from its current location object to a
     * destination location object
     * @param player the player object
     * @param currentLocation the current location object
     * @param gotoLocation the destination location object
     * @return the string to be passed to the client
     */
    private String gotoLocation(final Player player,
                                final Location currentLocation,
                                final Location gotoLocation) {
        if (currentLocation.pathToLocationExists(gotoLocation.getName().toLowerCase(Locale.ENGLISH))) {
            gotoLocation.addCharacter(player);
            currentLocation.removeCharacter(player);
            return gotoLocation.getArrivalString(player);
        }
        return "ERROR - " + player.getName() + " could not go to " +
            gotoLocation.getName() + " as no valid path exists\n";
    }
    
    /**
     * finds an artefact object from its name in an array of words provided
     * by the user
     * @param words array of words provided by the user
     * @param startIndex index where search starts from
     * @return the Artefact object, or an error object if there are more than
     * one artefact name in the array, or null if there are none
     */
    private Artefact findSingleArtefact(final String[] words,
                                        final int startIndex) {
        Artefact gottenArtefact = null;
        final Artefact err = new Artefact("ERROR", "ERROR");
        
        for (int jndex = startIndex; jndex < words.length; jndex++) {
            final String word = words[jndex];
            for (final GameEntity entity : entities) {
                if (word.equals(entity.getName().toLowerCase(Locale.ENGLISH)) &&
                    entity instanceof Artefact && gottenArtefact == null) {
                    gottenArtefact = (Artefact)entity;
                } else if (word.equals(entity.getName().toLowerCase(Locale.ENGLISH))) {
                    gottenArtefact = err;
                }
            }
        }
        
        return gottenArtefact;
    }
}
