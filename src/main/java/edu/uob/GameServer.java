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
    ArrayList<Location> locations = new ArrayList<>();
    

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
                if (location.getName().equals(startName)) {
                    start = location;
                } else if (location.getName().equals(endName)) {
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
        String alphanumericInst = inst.toLowerCase().replaceAll("[^a-z]", "");
        String[] words = alphanumericInst.split(" ");
        String output = "";
        GameAction toPerform = null;
        for (String w : words) {
            switch (w) {
                case "inventory", "inv" -> {
                    String commandOutput = player.listItems();
                    if (commandOutput.length() != 0 && output.length() != 0) {
                        return "ERROR - ambiguous command";
                    } else if (commandOutput.length() != 0) {
                        output = commandOutput;
                    }
                }
                case "get" -> {
                    String commandOutput = player.getArtefactFromLocation(words,
                        playerLocation);
                    if (commandOutput.length() != 0 && output.length() != 0) {
                        return "ERROR - ambiguous command";
                    } else if (commandOutput.length() != 0) {
                        output = commandOutput;
                    }
                }
                case "drop" -> {
                    String commandOutput = player.dropItem(words, playerLocation);
                    if (commandOutput.length() != 0 && output.length() != 0) {
                        return "ERROR - ambiguous command";
                    } else if (commandOutput.length() != 0) {
                        output = commandOutput;
                    }
                }
                case "goto" -> {
                    String commandOutput = gotoLocation(words, player,
                        playerLocation);
                    if (commandOutput.length() != 0 && output.length() != 0) {
                        return "ERROR - ambiguous command";
                    } else if (commandOutput.length() != 0) {
                        output = commandOutput;
                    }
                }
                case "look" -> {
                    String commandOutput = playerLocation.lookAround(player);
                    if (commandOutput.length() != 0 && output.length() != 0) {
                        return "ERROR - ambiguous command";
                    } else if (commandOutput.length() != 0) {
                        output = commandOutput;
                    }
                }
                case "health" -> {
                    String commandOutput = player.reportHealth();
                    if (commandOutput.length() != 0 && output.length() != 0) {
                        return "ERROR - ambiguous command";
                    } else if (commandOutput.length() != 0) {
                        output = commandOutput;
                    }
                }
                default -> {
                    if (singleTriggerActions.containsKey(w)) {
                        if (output.length() != 0 && !singleTriggerActions.get(w).contains(toPerform)) {
                            return "ERROR - ambiguous command";
                        }
                        for (GameAction a : singleTriggerActions.get(w)) {
                            String actionOutput = tryAction(a, words, player,
                                playerLocation);
                            if (actionOutput.length() != 0 && output.length() != 0 && a != toPerform) {
                                return "ERROR - ambiguous command";
                            } else if (actionOutput.length() != 0) {
                                output = actionOutput;
                                toPerform = a;
                            }
                        }
                    }
                }
            }
        }
        
        for (ActionTuple tup : multiTriggerActions) {
            if (inst.contains(tup.getTrigger())) {
                if (output.length() != 0 && !tup.getActions().contains(toPerform)) {
                    return "ERROR = ambiguous command";
                }
                for (GameAction a : tup.getActions()) {
                    String actionOutput = tryAction(a, words, player,
                        playerLocation);
                    if (actionOutput.length() != 0 && output.length() != 0 && a != toPerform) {
                        return "ERROR - ambiguous command";
                    } else if (actionOutput.length() != 0) {
                        output = actionOutput;
                        toPerform = a;
                    }
                }
            }
        }
        
        return ((output.length() == 0) ? "ERROR - command does not contain " +
            "an executable action\n" : output);
    }
    
    private String gotoLocation(String[] words, Player p, Location l) {
        for (String s : words) {
            if (l.pathToLocationExists(s)) {
                for (Location other : locations) {
                    if (other.getName().equals(s)) {
                        other.addCharacter(p);
                        l.removeCharacter(p);
                        return other.getArrivalString(p);
                    }
                }
            }
        }
        return (p.getName() + " could not go to any of these locations as no" +
            " valid path exists\n");
    }
    
    private String tryAction(GameAction a, String[] words, Player p, Location l) throws IOException {
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
            return "";
        }
        
        for (String s : a.getConsumed()) {
            if (!p.itemHeld(s) && !l.artefactIsPresent(s) && !l.furnitureIsPresent(s) && !s.equals("health")) {
                return "";
            }
        }
        
        for (String s : a.getSubjects()) {
            if (!p.itemHeld(s) && !l.artefactIsPresent(s) && !l.furnitureIsPresent(s) && !l.characterIsPresent(s)) {
                return "";
            }
        }
        
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
