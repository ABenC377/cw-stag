package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static edu.uob.BasicCommandType.ERROR;
import static edu.uob.BasicCommandType.NULL;

public class CommandHandler {
    private final HashMap<String, HashSet<GameAction>> oneWordActions;
    private final ArrayList<ActionTuple> manyWordActions;
    private final Location startLocation;
    private final Location storeRoom;
    private final ArrayList<Location> locations;
    private final ArrayList<GameEntity> entities;
    
    
    public CommandHandler(Location start,
                          Location store,
                          ArrayList<Location> locs,
                          ArrayList<GameEntity> ents,
                          HashMap<String, HashSet<GameAction>> oneWord,
                          ArrayList<ActionTuple> manyWord) {
        startLocation = start;
        storeRoom = store;
        locations = locs;
        entities = ents;
        oneWordActions = oneWord;
        manyWordActions = manyWord;
    }
    
    public String handle(final String command) throws IOException {
        final String[] components = command.split(":", 2);
        final String instruction = (components.length == 2) ? components[1] :
            "";
        if ("".equals(instruction)) {
            throw new IOException("ERROR: invalid command format");
        }
        
        // Set up player metadata
        Player player = null;
        Location playerLocation = null;
        final String userName = components[0];
        for (final Location location : locations) {
            final ArrayList<GameCharacter> characters =
                location.getCharacters();
            for (final GameCharacter c : characters) {
                if (c.getName().equals(userName)) {
                    player = (Player)c;
                    playerLocation = location;
                }
            }
        }
        if (player == null) {
            player = new Player(userName);
            entities.add(player);
            startLocation.addCharacter(player);
            playerLocation = startLocation;
        }
        
        return handleInstruction(instruction, player, playerLocation);
    }
    
    private String handleInstruction(final String inst, final Player player,
                                     final Location playerLocation) throws IOException {
        // Clean and parse command string
        final String[] words = cleanInstructions(inst);
        
        // Check for built-in commands and actions
        final BasicCommandType command = checkBasicCommands(words);
        GameAction gameAction = checkSingleTriggerActions(words, player,
            playerLocation);
        gameAction = checkMultiTriggerActions(inst, player, playerLocation,
            gameAction);
        
        // Check for errors
        if (command == ERROR || (gameAction != null && gameAction.getNarration().equals(
            "ERROR"))) {
            return "ERROR - invalid/ambiguous command\n";
        }
        
        // return an appropriate output
        if (command == NULL && gameAction == null) {
            return "ERROR - no valid instruction in that command";
        } else {
            return (command == NULL) ?
                handleAction(gameAction, player, playerLocation) :
                handleBasicCommand(command, player, playerLocation, words);
        }
    }
    
    private BasicCommandType checkBasicCommands(final String[] words) {
        BasicCommandType output = NULL;
        for (final String w : words) {
            if (BasicCommandType.fromString(w) != NULL && output == NULL) {
                output = BasicCommandType.fromString(w);
            } else if (BasicCommandType.fromString(w) != NULL) {
                output = ERROR;
            }
        }
        return output;
    }
    
    private GameAction checkSingleTriggerActions(final String[] words,
                                                 final Player player,
                                                 final Location location) {
        GameAction output = null;
        final GameAction err = new GameAction();
        err.setNarration("ERROR");
        
        // Handle single-word triggers
        for (final String word : words) {
            // Move straight on if word not a trigger
            if (!oneWordActions.containsKey(word)) {
                continue;
            }
            
            for (final GameAction action : oneWordActions.get(word)) {
                if (action.isDoable(words, player, location, entities) &&
                    (output == null || output == action)) {
                    output = action;
                } else if (action.isDoable(words, player, location, entities)) {
                    return err;
                }
            }
            
        }
        
        return output;
    }
    
    private GameAction checkMultiTriggerActions(final String inst,
                                                final Player player,
                                                final Location location,
                                                final GameAction current) {
        // Set up variables
        final String[] words = cleanInstructions(inst);
        GameAction output = current;
        final GameAction err = new GameAction();
        err.setNarration("ERROR");
        
        for (final ActionTuple tuple : manyWordActions) {
            // Move on if trigger not in instruction
            if (!inst.toLowerCase().contains(tuple.getTrigger())) {
                continue;
            }
            
            for (final GameAction action : tuple.getActions()) {
                // Check action is allowable
                if (output != null && output != action &&
                    action.isDoable(words, player, location, entities)) {
                    return err;
                } else if (action.isDoable(words, player, location, entities)) {
                    output = action;
                }
            }
        }
        return output;
    }
    
    private String[] cleanInstructions(final String inst) {
        final String alphanumericInst = inst.toLowerCase().replaceAll("[^a-zA" +
                "-Z0-9 ]",
            "");
        return alphanumericInst.split(" ");
    }
    
    private String handleAction(final GameAction action,
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
    
    private String handleBasicCommand(final BasicCommandType commandType,
                                      final Player player,
                                      final Location location,
                                      final String[] words) throws IOException {
        switch (commandType) {
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
        if (gottenArtefact.getName().equals("ERROR")) {
            return "ERROR - get command requires only one argument";
        }
        
        if (gottenArtefact == null ||
            !location.artefactIsPresent(gottenArtefact)) {
            return (gottenArtefact == null) ?
                "ERROR - get command requires an artefact name as an " +
                    "argument" :
                ("ERROR - " + gottenArtefact.getName() + " is not present " +
                    "in " + location.getName() + "\n");
        }
        
        location.removeArtefact(gottenArtefact);
        player.pickUpItem(gottenArtefact);
        return (player.getName() + " picked up " + gottenArtefact.getName() +
            "\n");
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
}
