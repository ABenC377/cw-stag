package edu.uob;

import java.io.IOException;
import java.util.*;

import static edu.uob.BasicCommandType.ERROR;
import static edu.uob.BasicCommandType.NULL;

/**
 * object for handling a user's command
 */
public class CommandHandler {
    /**
     * a hashmap with single-word triggers as key, and an arraylist of
     * corresponding action objects as value
     */
    private final Map<String, Set<GameAction>> oneWordActions;
    /**
     * list of tuples for actions that have triggers of more than one word
     */
    private final List<ActionTuple> manyWordActions;
    /**
     * location object for where a player starts the game
     */
    private final Location startLocation;
    /**
     * list of all the locations in the game
     */
    private final List<Location> locations;
    /**
     * list of all the entities in the game
     */
    private final List<GameEntity> entities;
    /**
     * an object for dealing with built-in commands
     */
    private final BasicCommandHandler basicHandler;
    /**
     * an object for dealing with custom actions
     */
    private final ActionHandler actionHandler;
    
    /**
     * @param start game start location
     * @param store the storeroom where currently non-existent entities live
     * @param locs list of locations in the game
     * @param ents list of entities in the game
     * @param oneWord hashmap of trigger and actions for one-word triggers
     * @param manyWord list of trigger-action tuples for multi-word triggers
     */
    public CommandHandler(final Location start,
                          final Location store,
                          final List<Location> locs,
                          final List<GameEntity> ents,
                          final Map<String, Set<GameAction>> oneWord,
                          final List<ActionTuple> manyWord) {
        startLocation = start;
        locations = locs;
        entities = ents;
        oneWordActions = oneWord;
        manyWordActions = manyWord;
        basicHandler = new BasicCommandHandler(entities);
        actionHandler = new ActionHandler(locations,
            startLocation, store);
    }
    
    /**
     * handles a user's input command
     * @param command the string provided by the client
     * @return the string to be returned to the client
     * @throws IOException self-explanatory
     */
    public String handle(final String command) throws IOException {
        final String[] components = command.split(":");
        final String instruction = (components.length == 2) ? components[1] :
            "";
        if ("".equals(instruction)) {
            return "ERROR: invalid command format";
        }
        
        if (userNameInvalid(components[0])) {
            return "ERROR: invalid username";
        }
        
        // Set up player metadata
        Player player = null;
        Location playerLocation = null;
        final String userName = components[0];
        for (final Location location : locations) {
            final List<GameCharacter> characters =
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
    
    /**
     * checks whether the provided username consists of only valid characters
     * @param username the proposed username
     * @return yes/no
     */
    private boolean userNameInvalid(String username) {
        final boolean matches = username.matches(".*[^ a-zA-Z1-9'-].*");
        return matches;
    }
    
    /**
     * handles the command part of the client-provided string
     * @param inst the command string
     * @param player the player object
     * @param playerLocation the player's location object
     * @return the string to be passed back to the client
     * @throws IOException self-explanatory
     */
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
        if ((command == ERROR) || ((gameAction != null) &&
            "ERROR".equals(gameAction.getNarration()))) {
            return "ERROR - invalid/ambiguous command\n";
        }
        
        // return an appropriate output
        if (command == NULL && gameAction == null) {
            return "ERROR - no valid instruction in that command";
        } else {
            return (command == NULL) ?
                actionHandler.handle(gameAction, player, playerLocation) :
                basicHandler.handle(command, player, playerLocation, words);
        }
    }
    
    /**
     * checks for the presence of a built-in command
     * @param words the words that make up the user's instruction
     * @return the type of command present (NULL if none, ERROR if more than
     * one)
     */
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
                    (output == null || output.equals(action))) {
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
            if (!inst.toLowerCase(Locale.ENGLISH).contains(tuple.getTrigger())) {
                continue;
            }
            
            for (final GameAction action : tuple.getActions()) {
                // Check action is allowable
                if (action.isDoable(words, player, location, entities)) {
                    if (output != null && !output.equals(action)) {
                        return err;
                    } else {
                        output = action;
                    }
                }
            }
        }
        return output;
    }
    
    private String[] cleanInstructions(final String inst) {
        final String alphanumericInst = inst.toLowerCase(Locale.ENGLISH).replaceAll("[^a" +
                "-zA" +
                "-Z0-9 ]",
            "");
        return alphanumericInst.split(" ");
    }
}
