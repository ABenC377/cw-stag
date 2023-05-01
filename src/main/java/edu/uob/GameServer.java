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

/** This class implements the STAG server. */
public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    HashMap<String, HashSet<GameAction>> singleTriggerActions;
    ArrayList<ActionTuple> multiTriggerActions = new ArrayList<>();
    
    Location startLocation;
    Location storeRoom;
    private final ArrayList<Location> locations = new ArrayList<>();
    private final ArrayList<GameEntity> entities = new ArrayList<>();
    
    
    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "extended" +
            "-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended" +
            "-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
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
    
    // TODO this is filthy long.  Refactor
    private HashMap<String, HashSet<GameAction>> readActionsFile(File file) throws ParserConfigurationException, IOException, SAXException {
        HashMap<String, HashSet<GameAction>> actionsHashMap = new HashMap<>();
        
        DocumentBuilder builder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = builder.parse(file);
        Element actions = d.getDocumentElement();
        NodeList actionNodeList = actions.getChildNodes();
        for (int i = 0; i < actionNodeList.getLength(); i++) {
            if ((i&1) == 1) {
                Element currentAction = (Element)actionNodeList.item(i);
                ArrayList<String> subjects = new ArrayList<>();
                ArrayList<String> consumed = new ArrayList<>();
                ArrayList<String> produced = new ArrayList<>();
                String narration;
                
                Element subjectsElement =
                    (Element)currentAction.getElementsByTagName("subjects").item(0);
                NodeList subjectsNL = subjectsElement.getElementsByTagName(
                    "entity");
                for (int j = 0; j < subjectsNL.getLength(); j++) {
                    Element subjectElement = (Element)subjectsNL.item(j);
                    subjects.add(subjectElement.getTextContent());
                }
                
                Element consumedsElement =
                    (Element)currentAction.getElementsByTagName("consumed").item(0);
                NodeList consumedsNL = consumedsElement.getElementsByTagName(
                    "entity");
                for (int j = 0; j < consumedsNL.getLength(); j++) {
                    Element consumedElement = (Element)consumedsNL.item(j);
                    consumed.add(consumedElement.getTextContent());
                }
                
                Element producedsElement =
                    (Element)currentAction.getElementsByTagName("produced").item(0);
                NodeList producedsNL = producedsElement.getElementsByTagName(
                    "entity");
                for (int j = 0; j < producedsNL.getLength(); j++) {
                    Element producedElement = (Element)producedsNL.item(j);
                    produced.add(producedElement.getTextContent());
                }
                
                Element narrationElement =
                    (Element)currentAction.getElementsByTagName("narration").item(0);
                narration = narrationElement.getTextContent();
                
                GameAction current = new GameAction(subjects, consumed, produced,
                    narration);
                
                Element triggersElement =
                    (Element)currentAction.getElementsByTagName(
                        "triggers").item(0);
                NodeList triggers = triggersElement.getElementsByTagName(
                    "keyphrase");
                for (int j = 0; j < triggers.getLength(); j++) {
                    Element triggerElement = (Element)triggers.item(j);
                    String trigger = triggerElement.getTextContent();
                    if (trigger.indexOf(' ') == -1) {
                        if (actionsHashMap.containsKey(trigger)) {
                            actionsHashMap.get(trigger).add(current);
                        } else {
                            HashSet<GameAction> hs = new HashSet<>();
                            hs.add(current);
                            actionsHashMap.put(trigger, hs);
                        }
                    } else {
                        boolean exists = false;
                        for (ActionTuple multiTriggerAction : multiTriggerActions) {
                            if (multiTriggerAction.getTrigger().equals(trigger)) {
                                multiTriggerAction.addAction(current);
                                exists = true;
                            }
                        }
                        if (!exists) {
                            ActionTuple at = new ActionTuple(trigger);
                            at.addAction(current);
                            multiTriggerActions.add(at);
                        }
                    }
                }
            }
        }
        return actionsHashMap;
    }
    
    
    private void readEntitiesFile(File file) throws FileNotFoundException, ParseException {
        Parser parser = new Parser();
        FileReader reader = new FileReader(file);
        parser.parse(reader);
        ArrayList<Graph> graphs = parser.getGraphs().get(0).getSubgraphs();
        
        
        // Get the locations to start with
        Graph locationsGraph = graphs.get(0);
        ArrayList<Graph> locationSubGraphs = locationsGraph.getSubgraphs();
        for (int i = 0; i < locationSubGraphs.size(); i++) {
            Graph current = locationSubGraphs.get(i);
            
            String locationName =
                current.getNodes(false).get(0).getId().getId();
            String locationDescription =
                current.getNodes(false).get(0).getAttribute("description");
            Location l = new Location(locationName, locationDescription);
            entities.add(l);
            
            ArrayList<Graph> contents = current.getSubgraphs();
            
            for (Graph content : contents) {
                String type = content.getId().getId();
                switch (type) {
                    case "artefacts" -> {
                        ArrayList<Node> artefactNodes =
                            content.getNodes(false);
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
                            content.getNodes(false);
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
                            content.getNodes(false);
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
            
            locations.add(l);
            if (i == 0) {
                startLocation = l;
            }
            if (locationName.equals("storeroom")) {
                storeRoom = l;
            }
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
            for (Location location : locations) {
                if (location.getName().toLowerCase().equals(startName)) {
                    start = location;
                } else if (location.getName().toLowerCase().equals(endName)) {
                    end = location;
                }
            }
            if (start != null) {
                start.addPath(end);
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
            startLocation.addCharacter(p);
            playerLocation = startLocation;
        }
        
        return handleInstruction(instruction, p, playerLocation);
    }
    
    // TODO - when dealing with ambiguous commands, this currently carries
    //  one of them out in the try___() methods.  Need to think about this.
    private String handleInstruction(String inst, Player player,
                                     Location playerLocation) throws IOException {
        String alphanumericInst = inst.toLowerCase().replaceAll("[^a-zA-Z0-9 ]",
            "");
        String[] words = alphanumericInst.split(" ");
        BasicCommand command = null;
        GameAction action = null;
        for (String w : words) {
            if (w.equals("inventory") || w.equals("inv")) {
                if (action != null || (command != null && command != BasicCommand.INV)) {
                    return "ERROR - ambiguous command";
                } else {
                    command = BasicCommand.INV;
                }
            } else if (w.equals("get")) {
                if (action != null || (command != null && command != BasicCommand.GET)) {
                    return "ERROR - ambiguous command";
                } else {
                    command = BasicCommand.GET;
                }
            } else if (w.equals("drop")) {
                if (action != null || (command != null && command != BasicCommand.DROP)) {
                    return "ERROR - ambiguous command";
                } else {
                    command = BasicCommand.DROP;
                }
            } else if (w.equals("goto")) {
                System.out.println("Found goto in the command\n");
                if (action != null || (command != null && command != BasicCommand.GOTO)) {
                    return "ERROR - ambiguous command";
                } else {
                    command = BasicCommand.GOTO;
                }
            } else if (w.equals("look")) {
                if (action != null || (command != null && command != BasicCommand.LOOK)) {
                    return "ERROR - ambiguous command";
                } else {
                    command = BasicCommand.LOOK;
                }
            } else if (w.equals("health")) {
                if (action != null || (command != null && command != BasicCommand.HEALTH)) {
                    return "ERROR - ambiguous command";
                } else {
                    command = BasicCommand.HEALTH;
                }
            } else {
                if (singleTriggerActions.containsKey(w)) {
                    if (command != null || (action != null && !singleTriggerActions.get(w).contains(action))) {
                        return "ERROR - ambiguous command";
                    }
                    for (GameAction a : singleTriggerActions.get(w)) {
                        if (actionIsValid(a, words, player,
                            playerLocation)) {
                            if (action != null && action != a) {
                                return "ERROR - ambiguous command";
                            }
                            action = a;
                        }
                    }
                }
            }
        }
        
        for (ActionTuple tup : multiTriggerActions) {
            if (alphanumericInst.contains(tup.getTrigger())) {
                if (command != null ||
                    action != null && !tup.getActions().contains(action)) {
                    return "ERROR = ambiguous command";
                }
                for (GameAction a : tup.getActions()) {
                    if (actionIsValid(a, words, player,
                        playerLocation)) {
                        if (action != null && action != a) {
                            return "ERROR - ambiguous command";
                        }
                        action = a;
                    }
                }
            }
        }
        
        if (command != null) {
            return handleBasicCommand(command, player, playerLocation, words);
        } else if (action != null) {
            return handleAction(action, player, playerLocation);
        } else {
            return "ERROR - no valid instruction in that command";
        }
    }
    
    private boolean actionIsValid(GameAction a, String[] words, Player p,
                                  Location l) {
        boolean present = false;
        for (String subj : a.getSubjects()) {
            for (String word : words) {
                if (subj.equals(word)) {
                    present = true;
                    break;
                }
            }
        }
        if (!present) {
            return false;
        }
        
        for (String s : a.getConsumed()) {
            if (!p.itemHeld(s) && !l.artefactIsPresent(s) && !l.furnitureIsPresent(s) && !s.equals("health")) {
                return false;
            }
        }
        
        for (String s : a.getSubjects()) {
            if (!p.itemHeld(s) && !l.artefactIsPresent(s) && !l.furnitureIsPresent(s) && !l.characterIsPresent(s)) {
                return false;
            }
        }
        
        return true;
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
    
    private String handleBasicCommand(BasicCommand c, Player p,
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
        return null;
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
                if (getIndex != -1) {
                    return "ERROR - invalid command, too many triggers for " +
                        "get command\n";
                } else {
                    getIndex = i;
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
                if (getIndex != -1) {
                    return "ERROR - invalid command, too many triggers for " +
                        "drop command\n";
                } else {
                    getIndex = i;
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
                if (getIndex != -1) {
                    return "ERROR - invalid command, too many triggers for " +
                        "drop command\n";
                } else {
                    getIndex = i;
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
        return (p.getName() + " could not go to " + gotoLocation.getName() +
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
