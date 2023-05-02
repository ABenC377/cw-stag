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
    private final ArrayList<Location> locations;
    private final ArrayList<GameEntity> entities;
    private final BasicCommandHandler commandHandler;
    private final ActionHandler actionHandler;
    
    
    public CommandHandler(final Location start,
                          final Location store,
                          final ArrayList<Location> locs,
                          final ArrayList<GameEntity> ents,
                          final HashMap<String, HashSet<GameAction>> oneWord,
                          final ArrayList<ActionTuple> manyWord) {
        startLocation = start;
        locations = locs;
        entities = ents;
        oneWordActions = oneWord;
        manyWordActions = manyWord;
        commandHandler = new BasicCommandHandler(entities);
        actionHandler = new ActionHandler(locations,
            startLocation, store);
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
                actionHandler.handle(gameAction, player, playerLocation) :
                commandHandler.handle(command, player, playerLocation, words);
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
}
