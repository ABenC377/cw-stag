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
    private HashMap<String, HashSet<GameAction>> singleTriggerActions;
    private ArrayList<ActionTuple> multiTriggerActions = new ArrayList<>();
    
    private Location startLocation = null;
    private Location storeRoom;
    private final ArrayList<Location> locations = new ArrayList<>();
    private final ArrayList<GameEntity> entities = new ArrayList<>();
    
    
    public static void main(String[] args) throws IOException {
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
    public GameServer(File entitiesFile, File actionsFile) {
        // TODO implement your server logic here
        try {
            singleTriggerActions = readActionsFile(actionsFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        
        try {
            readEntitiesFile(entitiesFile);
        } catch (FileNotFoundException | ParseException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    private HashMap<String, HashSet<GameAction>> readActionsFile(File file) throws ParserConfigurationException, IOException, SAXException {
        HashMap<String, HashSet<GameAction>> actionsHashMap = new HashMap<>();
        
        DocumentBuilder builder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = builder.parse(file);
        Element actions = d.getDocumentElement();
        NodeList actionNodeList = actions.getChildNodes();
        // Weird for loop, as we only want the odd elements
        for (int i = 1; i < actionNodeList.getLength(); i += 2) {
            Element currentAction = (Element)actionNodeList.item(i);
                
            GameAction current = new GameActionBuilder().createGameAction();
                
            addSubjects(currentAction, current);
            addConsumed(currentAction, current);
            addProduced(currentAction, current);
                
            Element narrationElement =
                (Element)currentAction.getElementsByTagName("narration").item(0);
            current.setNarration(narrationElement.getTextContent());
            
            Element triggersElement =
                (Element)currentAction.getElementsByTagName(
                    "triggers").item(0);
            NodeList triggers = triggersElement.getElementsByTagName(
                "keyphrase");
            
            addActionsByTrigger(actionsHashMap, current, triggers);
        }
        
        return actionsHashMap;
    }
    
    private void addSubjects(Element e, GameAction a) {
        Element subElement =
            (Element)e.getElementsByTagName("subjects").item(0);
        NodeList subjectsNL = subElement.getElementsByTagName(
            "entity");
        for (int i = 0; i < subjectsNL.getLength(); i++) {
            Element subjectElement = (Element)subjectsNL.item(i);
            a.addSubject(subjectElement.getTextContent());
        }
    }
    
    private void addConsumed(Element e, GameAction a) {
        Element consElement =
            (Element)e.getElementsByTagName("consumed").item(0);
        NodeList consumedsNL = consElement.getElementsByTagName(
            "entity");
        for (int i = 0; i < consumedsNL.getLength(); i++) {
            Element consumedElement = (Element)consumedsNL.item(i);
            a.addConsumed(consumedElement.getTextContent());
        }
    }
    
    private void addProduced(Element e, GameAction a) {
        Element prodElement =
            (Element)e.getElementsByTagName("produced").item(0);
        NodeList producedsNL = prodElement.getElementsByTagName(
            "entity");
        for (int i = 0; i < producedsNL.getLength(); i++) {
            Element producedElement = (Element)producedsNL.item(i);
            a.addProduced(producedElement.getTextContent());
        }
    }
    
    private void addActionsByTrigger(HashMap<String, HashSet<GameAction>> hm,
                                     GameAction a, NodeList nl) {
        for (int i = 0; i < nl.getLength(); i++) {
            Element triggerElement = (Element)nl.item(i);
            String trigger = triggerElement.getTextContent();
            if (trigger.indexOf(' ') == -1) {
                if (hm.containsKey(trigger)) {
                    hm.get(trigger).add(a);
                } else {
                    HashSet<GameAction> hs = new HashSet<>();
                    hs.add(a);
                    hm.put(trigger, hs);
                }
            } else {
                addToMultiTriggers(trigger, a);
            }
        }
    }
    
    private void addToMultiTriggers(String s, GameAction a) {
        boolean exists = false;
        for (ActionTuple multiTriggerAction : multiTriggerActions) {
            if (multiTriggerAction.getTrigger().equals(s)) {
                multiTriggerAction.addAction(a);
                exists = true;
            }
        }
        if (!exists) {
            ActionTuple at = new ActionTuple(s);
            at.addAction(a);
            multiTriggerActions.add(at);
        }
    }
    
    
    private void readEntitiesFile(File file) throws FileNotFoundException, ParseException {
        Parser parser = new Parser();
        FileReader reader = new FileReader(file);
        parser.parse(reader);
        ArrayList<Graph> graphs = parser.getGraphs().get(0).getSubgraphs();
        
        
        // Get the locations to start with
        Graph locationsGraph = graphs.get(0);
        ArrayList<Graph> locationSubGraphs = locationsGraph.getSubgraphs();
        for (Graph current : locationSubGraphs) {
            addLocation(current);
            
        }
        
        // Then handle paths between them
        Graph pathsGraph = graphs.get(1);
        ArrayList<Edge> pathEdges = pathsGraph.getEdges();
        for (Edge pathEdge : pathEdges) {
            String startName =
                pathEdge.getSource().getNode().getId().getId();
            String endName =
                pathEdge.getTarget().getNode().getId().getId();
            Location start = null;
            Location end = null;
            for (Location l : locations) {
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
    
    private void addLocation(Graph g) {
        String locationName =
            g.getNodes(false).get(0).getId().getId();
        String locationDescription =
            g.getNodes(false).get(0).getAttribute("description");
        Location l = new Location(locationName, locationDescription);
        entities.add(l);
        
        ArrayList<Graph> contents = g.getSubgraphs();
        
        for (Graph content : contents) {
            addEntityToLocation(l, content);
        }
        
        locations.add(l);
        if (startLocation == null) {
            startLocation = l;
        }
        if (locationName.equals("storeroom")) {
            storeRoom = l;
        }
    }
    
    private void addEntityToLocation(Location l, Graph g) {
        String type = g.getId().getId();
        switch (type) {
            case "artefacts" -> {
                ArrayList<Node> artefactNodes =
                    g.getNodes(false);
                for (Node artefactNode : artefactNodes) {
                    Artefact a =
                        new Artefact(artefactNode.getId().getId(),
                            artefactNode.getAttribute("description"));
                    l.addArtefact(a);
                    entities.add(a);
                }
            }
            case "furniture" -> {
                ArrayList<Node> furnitureNodes =
                    g.getNodes(false);
                for (Node furnitureNode : furnitureNodes) {
                    Furniture f =
                        new Furniture(furnitureNode.getId().getId(),
                            furnitureNode.getAttribute(
                                "description"));
                    l.addFurniture(f);
                    entities.add(f);
                }
            }
            case "characters" -> {
                ArrayList<Node> characterNodes =
                    g.getNodes(false);
                for (Node characterNode : characterNodes) {
                    GameCharacter c =
                        new GameCharacter(characterNode.getId().getId(),
                            characterNode.getAttribute(
                                "description"));
                    l.addCharacter(c);
                    entities.add(c);
                }
            }
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming game commands and carries out the corresponding actions.
    */
    public String handleCommand(String command) throws IOException {
        // TODO implement your server logic here
        String[] components = command.split(":", 2);
        String userName = components[0];
        String instruction = (components.length == 2) ? components[1] : "";
        if (instruction.equals("")) {
            throw new IOException("ERROR: invalid command format");
        }
        
        // Set up player metadata
        Player p = null;
        Location playerLocation = null;
        for (Location l : locations) {
            ArrayList<GameCharacter> characters = l.getCharacters();
            for (GameCharacter c : characters) {
                if (c.getName().equals(userName)) {
                    p = (Player)c;
                    playerLocation = l;
                }
            }
        }
        if (p == null) {
            p = new Player(userName);
            entities.add(p);
            startLocation.addCharacter(p);
            playerLocation = startLocation;
        }
        
        return handleInstruction(instruction, p, playerLocation);
    }
    
    private String handleInstruction(String inst, Player player,
                                     Location playerLocation) throws IOException {
        String[] words = cleanInstructions(inst);
        BasicCommandType command = NULL;
        GameAction gameAction = null;
        
        // Handle single-word triggers
        for (String w : words) {
            // Check if word is a built-in command trigger
            if (BasicCommandType.fromString(w) != NULL && command == NULL && gameAction == null) {
                    command = BasicCommandType.fromString(w);
            } else if (BasicCommandType.fromString(w) != NULL) {
                return "ERROR - built-in commands cannot have more than " +
                    "one trigger";
            // Check if the word is an action trigger word
            } else if (singleTriggerActions.containsKey(w) && command == NULL && (gameAction == null || singleTriggerActions.get(w).contains(gameAction))) {
                for (GameAction a : singleTriggerActions.get(w)) {
                    if (a.isDoable(words, player, playerLocation) && gameAction == null || gameAction == a) {
                        gameAction = a;
                    } else if (a.isDoable(words, player, playerLocation)) {
                        return "ERROR - ambiguous command\n";
                    }
                }
            }
        }
        
        // Handle multi-word triggers
        for (ActionTuple tup : multiTriggerActions) {
            if (inst.toLowerCase().contains(tup.getTrigger())) {
                if (command != NULL ||
                    (gameAction != null && !tup.getActions().contains(gameAction))) {
                    return "ERROR = ambiguous command";
                }
                for (GameAction a : tup.getActions()) {
                    if (a.isDoable(words, player,
                        playerLocation)) {
                        if (gameAction != null && gameAction != a) {
                            return "ERROR - ambiguous command";
                        }
                        gameAction = a;
                    }
                }
            }
        }
        
        if (command != NULL) {
            return handleBasicCommand(command, player, playerLocation, words);
        } else if (gameAction != null) {
            return handleAction(gameAction, player, playerLocation);
        } else {
            return "ERROR - no valid instruction in that command";
        }
    }
    
    private String[] cleanInstructions(String inst) {
        String alphanumericInst = inst.toLowerCase().replaceAll("[^a-zA-Z0-9 ]",
            "");
        return alphanumericInst.split(" ");
    }
    
    private String handleAction(GameAction a, Player p,
                                Location l) throws IOException {
        for (String s : a.getConsumed()) {
            if (p.itemHeld(s)) {
                Artefact i = p.getItem(s);
                p.removeItem(i);
                storeRoom.addArtefact(i);
            } else if (l.artefactIsPresent(s)) {
                storeRoom.addArtefact(l.removeArtefact(s));
            } else if (l.furnitureIsPresent(s)) {
                storeRoom.addFurniture(l.removeFurniture(s));
            } else if (s.equals("health")) {
                p.takeDamage();
            }
        }
        
        for (String s : a.getProduced()) {
            if (s.equals("health")) {
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
    
    private String handleBasicCommand(BasicCommandType c, Player p,
                                      Location l, String[] words) throws IOException {
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
        }
        return "ERROR - not a valid basic command type";
    }
    
    private String handleInv(String[] words, Player p) {
        boolean invAlreadySeen = false;
        for (String w : words) {
            if (w.equals("inv") || w.equals("inventory")) {
                if (invAlreadySeen) {
                    return "ERROR - invalid command, too many triggers for " +
                        "inventory command\n";
                } else {
                    invAlreadySeen = true;
                }
            }
        }
        
        for (String w : words) {
            for (GameEntity e : entities) {
                if (e.getName().toLowerCase().equals(w)) {
                    return "ERROR - cannot use entity name as decoration for " +
                        "inventory command\n";
                }
            }
        }
        
        return p.listItems();
    }
    
    private String handleGet(String[] words, Player p, Location l) {
        int getIndex = -1;
        int i = 0;
        for (String w : words) {
            if (w.equals("get")) {
                if (getIndex == -1) {
                    getIndex = i;
                } else {
                    return "ERROR - invalid command, too many triggers for " +
                        "get command\n";
                }
            }
            i++;
        }
        
        Artefact gottenArtefact = null;
        for (int j = getIndex + 1; j < words.length; j++) {
            String w = words[j];
            for (GameEntity e : entities) {
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
    
    private String handleDrop(String[] words, Player p, Location l) throws IOException {
        int getIndex = -1;
        int i = 0;
        for (String w : words) {
            if (w.equals("drop")) {
                if (getIndex == -1) {
                    getIndex = i;
                } else {
                    return "ERROR - invalid command, too many triggers for " +
                        "drop command\n";
                }
            }
            i++;
        }
        
        Artefact droppedArtefact = null;
        for (int j = getIndex + 1; j < words.length; j++) {
            String w = words[j];
            for (GameEntity e : entities) {
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
    
    private String handleGoto(String[] words, Player p, Location l) {
        int getIndex = -1;
        int i = 0;
        for (String w : words) {
            if (w.equals("goto")) {
                if (getIndex == -1) {
                    getIndex = i;
                } else {
                    return "ERROR - invalid command, too many triggers for " +
                        "drop command\n";
                }
            }
            i++;
        }
        
        Location gotoLocation = null;
        for (int j = getIndex; j < words.length; j++) {
            String w = words[j];
            for (GameEntity e : entities) {
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
    
    private String gotoLocation(Player p, Location currentLocation,
                                Location gotoLocation) {
        if (currentLocation.pathToLocationExists(gotoLocation.getName().toLowerCase())) {
            gotoLocation.addCharacter(p);
            currentLocation.removeCharacter(p);
            return gotoLocation.getArrivalString(p);
        }
        return ("ERROR - " + p.getName() + " could not go to " + gotoLocation.getName() +
            " as no valid path exists\n");
    }
    
    private String handleLook(String[] words, Player p, Location l) {
        boolean looked = false;
        for (String w : words) {
            if (w.equals("look")) {
                if (looked) {
                    return "ERROR - invalid command, too many triggers for " +
                        "look command\n";
                } else {
                    looked = true;
                }
            }
            
            for (GameEntity e : entities) {
                if (w.equals(e.getName().toLowerCase())) {
                    return "ERROR - look requires no arguments, so the " +
                        "command cannot contain any entity names\n";
                }
            }
        }
        
        return l.lookAround(p);
    }
    
    private String handleHealth(String[] words, Player p) {
        boolean healthed = false;
        for (String w : words) {
            if (w.equals("health")) {
                if (healthed) {
                    return "ERROR - invalid command, too many triggers for " +
                        "health command";
                }
            }
            
            for (GameEntity e : entities) {
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
    public void blockingListenOn(int portNumber) throws IOException {
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
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
