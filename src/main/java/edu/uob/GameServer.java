package edu.uob;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;

import static edu.uob.BasicCommandType.*;

/** This class implements the STAG server. */
public final class GameServer {
    
    // TODO add tests for edge case of an action with no subjects - should
    //  still be able to work even though no subjects given in command
    
    // TODO test multiplayer see the other player in a room

    private static final char END_OF_TRANSMISSION = 4;
    private HashMap<String, HashSet<GameAction>> oneWordActions;
    private ArrayList<ActionTuple> manyWordActions = new ArrayList<>();
    
    private Location startLocation = null;
    private Location storeRoom;
    private final ArrayList<Location> locations = new ArrayList<>();
    private final ArrayList<GameEntity> entities = new ArrayList<>();
    
    
    public static void main(final String[] args) throws IOException {
        final File entitiesFile = Paths.get("config" + File.separator +
            "extended" +
            "-entities.dot").toAbsolutePath().toFile();
        final File actionsFile = Paths.get("config" + File.separator +
            "extended" +
            "-actions.xml").toAbsolutePath().toFile();
        final GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer(File, File)}) otherwise we won't be able to mark
    * your submission correctly.
    *
    * <p>You MUST use the supplied {@code entitiesFile} and {@code actionsFile}
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    *
    */
    public GameServer(final File entitiesFile, final File actionsFile) {
        // TODO implement your server logic here
        try {
            oneWordActions = readActionsFile(actionsFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        
        try {
            readEntitiesFile(entitiesFile);
        } catch (FileNotFoundException | ParseException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    private HashMap<String, HashSet<GameAction>> readActionsFile(final File file) throws ParserConfigurationException, IOException, SAXException {
        final HashMap<String, HashSet<GameAction>> actionsHashMap =
            new HashMap<>();
        final DocumentBuilder builder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = builder.parse(file);
        final Element actions = document.getDocumentElement();
        final NodeList actionNodeList = actions.getChildNodes();
        // Weird for loop, as we only want the odd elements
        for (int i = 1; i < actionNodeList.getLength(); i += 2) {
            final Element currentAction = (Element)actionNodeList.item(i);
                
            final GameAction current =
                new GameActionBuilder().createGameAction();
                
            addSubjects(currentAction, current);
            addConsumed(currentAction, current);
            addProduced(currentAction, current);
                
            final Element narrationElement =
                (Element)currentAction.getElementsByTagName("narration").item(0);
            current.setNarration(narrationElement.getTextContent());
            
            final Element triggersElement =
                (Element)currentAction.getElementsByTagName(
                    "triggers").item(0);
            final NodeList triggers = triggersElement.getElementsByTagName(
                "keyphrase");
            
            addActionsByTrigger(actionsHashMap, current, triggers);
        }
        
        return actionsHashMap;
    }
    
    private void addSubjects(final Element element, final GameAction action) {
        final Element subElement =
            (Element)element.getElementsByTagName("subjects").item(0);
        final NodeList subjectsNL = subElement.getElementsByTagName(
            "entity");
        for (int i = 0; i < subjectsNL.getLength(); i++) {
            final Element subjectElement = (Element)subjectsNL.item(i);
            action.addSubject(subjectElement.getTextContent());
        }
    }
    
    private void addConsumed(final Element element, final GameAction action) {
        final Element consElement =
            (Element)element.getElementsByTagName("consumed").item(0);
        final NodeList consumedsNL = consElement.getElementsByTagName(
            "entity");
        for (int i = 0; i < consumedsNL.getLength(); i++) {
            final Element consumedElement = (Element)consumedsNL.item(i);
            action.addConsumed(consumedElement.getTextContent());
        }
    }
    
    private void addProduced(final Element element, final GameAction action) {
        final Element prodElement =
            (Element)element.getElementsByTagName("produced").item(0);
        final NodeList producedsNL = prodElement.getElementsByTagName(
            "entity");
        for (int i = 0; i < producedsNL.getLength(); i++) {
            final Element producedElement = (Element)producedsNL.item(i);
            action.addProduced(producedElement.getTextContent());
        }
    }
    
    private void addActionsByTrigger(final HashMap<String,
        HashSet<GameAction>> triggerMap,
                                     final GameAction action,
                                     final NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            final Element triggerElement = (Element)nodes.item(i);
            final String trigger = triggerElement.getTextContent();
            if (trigger.indexOf(' ') == -1) {
                if (triggerMap.containsKey(trigger)) {
                    triggerMap.get(trigger).add(action);
                } else {
                    final HashSet<GameAction> actionSet = new HashSet<>();
                    actionSet.add(action);
                    triggerMap.put(trigger, actionSet);
                }
            } else {
                addToMultiTriggers(trigger, action);
            }
        }
    }
    
    private void addToMultiTriggers(final String trigger,
                                    final GameAction action) {
        boolean exists = false;
        for (final ActionTuple tup : manyWordActions) {
            if (tup.getTrigger().equals(trigger)) {
                tup.addAction(action);
                exists = true;
            }
        }
        if (!exists) {
            final ActionTuple tuple = new ActionTuple(trigger);
            tuple.addAction(action);
            manyWordActions.add(tuple);
        }
    }
    
    
    private void readEntitiesFile(final File file) throws FileNotFoundException, ParseException {
        final Parser parser = new Parser();
        final FileReader reader = new FileReader(file);
        parser.parse(reader);
        final ArrayList<Graph> graphs =
            parser.getGraphs().get(0).getSubgraphs();
        
        
        // Get the locations to start with
        final Graph locationsGraph = graphs.get(0);
        final ArrayList<Graph> locationSubGraphs =
            locationsGraph.getSubgraphs();
        for (final Graph current : locationSubGraphs) {
            addLocation(current);
            
        }
        
        // Then handle paths between them
        final Graph pathsGraph = graphs.get(1);
        final ArrayList<Edge> pathEdges = pathsGraph.getEdges();
        for (final Edge pathEdge : pathEdges) {
            final String startName =
                pathEdge.getSource().getNode().getId().getId();
            final String endName =
                pathEdge.getTarget().getNode().getId().getId();
            Location start = null;
            Location end = null;
            for (final Location l : locations) {
                if (l.getName().toLowerCase().equals(startName)) {
                    start = l;
                } else if (l.getName().toLowerCase().equals(endName)) {
                    end = l;
                }
            }
            if (start != null && end != null) {
                start.addPath(end);
            }
        }
    }
    
    private void addLocation(final Graph graph) {
        final String locationName =
            graph.getNodes(false).get(0).getId().getId();
        final String description =
            graph.getNodes(false).get(0).getAttribute("description");
        final Location location = new Location(locationName, description);
        entities.add(location);
        
        final ArrayList<Graph> contents = graph.getSubgraphs();
        
        for (final Graph content : contents) {
            addEntityToLocation(location, content);
        }
        
        locations.add(location);
        if (startLocation == null) {
            startLocation = location;
        }
        if ("storeroom".equals(locationName)) {
            storeRoom = location;
        }
    }
    
    private void addEntityToLocation(final Location location,
                                     final Graph graph) {
        final String type = graph.getId().getId();
        switch (type) {
            case "artefacts" -> {
                final ArrayList<Node> artefactNodes =
                    graph.getNodes(false);
                for (final Node artefactNode : artefactNodes) {
                    final Artefact artefact =
                        new Artefact(artefactNode.getId().getId(),
                            artefactNode.getAttribute("description"));
                    location.addArtefact(artefact);
                    entities.add(artefact);
                }
            }
            case "furniture" -> {
                final ArrayList<Node> furnitureNodes =
                    graph.getNodes(false);
                for (final Node furnitureNode : furnitureNodes) {
                    final Furniture furniture =
                        new Furniture(furnitureNode.getId().getId(),
                            furnitureNode.getAttribute(
                                "description"));
                    location.addFurniture(furniture);
                    entities.add(furniture);
                }
            }
            case "characters" -> {
                final ArrayList<Node> characterNodes =
                    graph.getNodes(false);
                for (final Node characterNode : characterNodes) {
                    final GameCharacter character =
                        new GameCharacter(characterNode.getId().getId(),
                            characterNode.getAttribute(
                                "description"));
                    location.addCharacter(character);
                    entities.add(character);
                }
            }
            default -> {}
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming game commands and carries out the corresponding actions.
    */
    public String handleCommand(final String command) throws IOException {
        // TODO implement your server logic here
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
                if (action.isDoable(words, player, location) &&
                    (output == null || output == action)) {
                    output = action;
                } else if (action.isDoable(words, player, location)) {
                    return err;
                }
            }
            
        }
        
        return output;
    }
    
    private GameAction checkMultiTriggerActions(final String inst, final Player p,
                                                final Location l,
                                                final GameAction current) {
        // Set up variables
        final String[] words = cleanInstructions(inst);
        GameAction output = current;
        final GameAction err = new GameAction();
        err.setNarration("ERROR");
        
        for (final ActionTuple tup : manyWordActions) {
            // Move on if trigger not in instruction
            if (!inst.toLowerCase().contains(tup.getTrigger())) {
                continue;
            }
            
            for (final GameAction a : tup.getActions()) {
                // Check action is allowable
                if (output != null && output != a && a.isDoable(words, p, l)) {
                    return err;
                } else if (a.isDoable(words, p, l)) {
                    output = a;
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
    
    private String handleAction(final GameAction a, final Player p,
                                final Location l) throws IOException {
        for (final String s : a.getConsumed()) {
            if (p.itemHeld(s)) {
                final Artefact i = p.getItem(s);
                p.removeItem(i);
                storeRoom.addArtefact(i);
            } else if (l.artefactIsPresent(s)) {
                storeRoom.addArtefact(l.removeArtefact(s));
            } else if (l.furnitureIsPresent(s)) {
                storeRoom.addFurniture(l.removeFurniture(s));
            } else if ("health".equals(s)) {
                p.takeDamage();
            }
        }
        
        for (final String s : a.getProduced()) {
            if ("health".equals(s)) {
                p.heal();
            } else {
                l.produce(s, locations);
            }
        }
        
        if (p.checkForDeath(l, startLocation)) {
            return (a.getNarration() +
                "\nYou pass out from the damage\n" +
                "You wake up in " +
                startLocation.getDescription() +
                " without any of your possessions\n");
        }
        
        return a.getNarration();
    }
    
    private String handleBasicCommand(final BasicCommandType c, final Player p,
                                      final Location l, final String[] words) throws IOException {
        switch (c) {
            case INV -> {
                return handleInv(words, p);
            }
            case GET -> {
                return handleGet(words, p, l);
            }
            case DROP -> {
                return handleDrop(words, p, l);
            }
            case GOTO -> {
                return handleGoto(words, p, l);
            }
            case LOOK -> {
                return handleLook(words, p, l);
            }
            case HEALTH -> {
                return handleHealth(words, p);
            }
            default -> {
                return "ERROR - not a valid basic command type";
            }
        }
    }
    
    private String handleInv(final String[] words, final Player p) {
        boolean invAlreadySeen = false;
        for (final String w : words) {
            if ("inv".equals(w) || "inventory".equals(w)) {
                if (invAlreadySeen) {
                    return "ERROR - invalid command, too many triggers for " +
                        "inventory command\n";
                } else {
                    invAlreadySeen = true;
                }
            }
        }
        
        for (final String w : words) {
            for (final GameEntity e : entities) {
                if (e.getName().toLowerCase().equals(w)) {
                    return "ERROR - cannot use entity name as decoration for " +
                        "inventory command\n";
                }
            }
        }
        
        return p.listItems();
    }
    
    private String handleGet(final String[] words, final Player p, final Location l) {
        final int getIndex = findIndex(words, "get");
        if (getIndex == -1) {
            return "ERROR - invalid command, too many triggers for " +
                "get command\n";
        }
        
        Artefact gottenArtefact = null;
        for (int j = getIndex + 1; j < words.length; j++) {
            final String w = words[j];
            for (final GameEntity e : entities) {
                if (w.equals(e.getName().toLowerCase())) {
                    if (e instanceof Artefact && gottenArtefact == null) {
                        gottenArtefact = (Artefact)e;
                    } else {
                        return "ERROR - get requires one artefact as its " +
                            "argument";
                    }
                }
            }
        }
        
        if (gottenArtefact == null) {
            return "ERROR - get command requires an artefact name as an " +
                "argument";
        }
        
        if (!l.artefactIsPresent(gottenArtefact)) {
            return ("ERROR - " + gottenArtefact.getName() + " is not present " +
                "in " + l.getName() + "\n");
        }
        
        l.removeArtefact(gottenArtefact);
        p.pickUpItem(gottenArtefact);
        return (p.getName() + " picked up " + gottenArtefact.getName() + "\n");
    }
    
    private int findIndex(final String[] words, final String toFind) {
        int output = -1;
        int i = 0;
        for (final String w : words) {
            if (w.equals(toFind) && output == -1) {
                output = i;
            } else if (w.equals(toFind)) {
                return -1;
            }
            i++;
        }
        return output;
    }
    
    private String handleDrop(final String[] words, final Player p, final Location l) throws IOException {
        final int getIndex = findIndex(words, "drop");
        if (getIndex == -1) {
            return "ERROR - invalid command, too many " +
            "triggers for drop command\n";
        }
        
        Artefact droppedArtefact = null;
        for (int j = getIndex + 1; j < words.length; j++) {
            final String w = words[j];
            for (final GameEntity e : entities) {
                if (w.equals(e.getName().toLowerCase())) {
                    if (e instanceof Artefact && droppedArtefact == null) {
                        droppedArtefact = (Artefact)e;
                    } else {
                        return "ERROR - drop requires one artefact as its " +
                            "argument";
                    }
                }
            }
        }
        
        if (droppedArtefact == null) {
            return "ERROR - drop command requires an artefact name as an " +
                "argument";
        }
        
        if (!p.itemHeld(droppedArtefact)) {
            return ("ERROR - cannot drop " + droppedArtefact.getName() + " as" +
                " it is not in your inventory\n");
        }
        
        p.removeItem(droppedArtefact);
        l.addArtefact(droppedArtefact);
        return (p.getName() + " dropped " + droppedArtefact.getName() + "\n");
    }
    
    private String handleGoto(final String[] words, final Player p, final Location l) {
        final int getIndex = findIndex(words, "goto");
        if (getIndex == -1) {
            return "ERROR - invalid command, too many triggers for " +
                        "drop command\n";
        }
        
        Location gotoLocation = null;
        for (int j = getIndex; j < words.length; j++) {
            final String w = words[j];
            for (final GameEntity e : entities) {
                if (w.equals(e.getName().toLowerCase())) {
                    if (e instanceof Location && gotoLocation == null) {
                        gotoLocation = (Location)e;
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
        
        return gotoLocation(p, l, gotoLocation);
    }
    
    private String gotoLocation(final Player p, final Location currentLocation,
                                final Location gotoLocation) {
        if (currentLocation.pathToLocationExists(gotoLocation.getName().toLowerCase())) {
            gotoLocation.addCharacter(p);
            currentLocation.removeCharacter(p);
            return gotoLocation.getArrivalString(p);
        }
        return ("ERROR - " + p.getName() + " could not go to " + gotoLocation.getName() +
            " as no valid path exists\n");
    }
    
    private String handleLook(final String[] words, final Player p, final Location l) {
        boolean looked = false;
        for (final String w : words) {
            if ("look".equals(w)) {
                if (looked) {
                    return "ERROR - invalid command, too many triggers for " +
                        "look command\n";
                } else {
                    looked = true;
                }
            }
            
            for (final GameEntity e : entities) {
                if (w.equals(e.getName().toLowerCase())) {
                    return "ERROR - look requires no arguments, so the " +
                        "command cannot contain any entity names\n";
                }
            }
        }
        
        return l.lookAround(p);
    }
    
    private String handleHealth(final String[] words, final Player p) {
        boolean healthed = false;
        for (final String w : words) {
            if ("health".equals(w)) {
                if (healthed) {
                    return "ERROR - invalid command, too many triggers for " +
                        "health command";
                } else {
                    healthed = true;
                }
            }
            
            for (final GameEntity e : entities) {
                if (w.equals(e.getName().toLowerCase())) {
                    return "ERROR - health requires no arguments, so the " +
                        "command cannot contain any entity names\n";
                }
            }
        }
        
        return p.reportHealth();
    }



    //  === Methods below are there to facilitate server related operations. ===

    /**
    * Starts a *blocking* socket server listening for new connections. This method blocks until the
    * current thread is interrupted.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * you want to.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(final int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Handles an incoming connection from the socket server.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * * you want to.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(final ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            final String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                final String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
