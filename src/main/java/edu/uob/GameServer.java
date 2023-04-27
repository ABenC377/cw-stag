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
    HashMap<String, HashSet<Action>> singleTriggerActions;
    ArrayList<ActionTuple> multiTriggerActions = new ArrayList<>();
    
    Location startLocation;
    Location storeRoom;
    ArrayList<Location> locations;
    

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
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
    private HashMap<String, HashSet<Action>> readActionsFile(File file) throws ParserConfigurationException, IOException, SAXException {
        HashMap<String, HashSet<Action>> actionsHashMap = new HashMap<>();
        int id = 1;
        
        DocumentBuilder builder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = builder.parse(file);
        Element actions = d.getDocumentElement();
        Element currentAction = (Element)actions.getFirstChild();
        while (currentAction != null) {
            ArrayList<String> subjects = new ArrayList<>();
            ArrayList<String> consumed = new ArrayList<>();
            ArrayList<String> produced = new ArrayList<>();
            String narration;
            
            Element subjectsElement =
                (Element)currentAction.getElementsByTagName("subjects").item(0);
            NodeList subjectsNL = subjectsElement.getElementsByTagName(
                "entity");
            for (int i = 0; i < subjectsNL.getLength(); i++) {
                Element subjectElement = (Element)subjectsNL.item(i);
                subjects.add(subjectElement.getTextContent());
            }
            
            Element consumedsElement =
                (Element)currentAction.getElementsByTagName("consumed").item(0);
            NodeList consumedsNL = consumedsElement.getElementsByTagName(
                "entity");
            for (int i = 0; i < consumedsNL.getLength(); i++) {
                Element consumedElement = (Element)consumedsNL.item(i);
                consumed.add(consumedElement.getTextContent());
            }
            
            Element producedsElement =
                (Element)currentAction.getElementsByTagName("produced").item(0);
            NodeList producedsNL = producedsElement.getElementsByTagName(
                "entity");
            for (int i = 0; i < producedsNL.getLength(); i++) {
                Element producedElement = (Element)producedsNL.item(i);
                produced.add(producedElement.getTextContent());
            }
            
            Element narrationElement =
                (Element)currentAction.getElementsByTagName("narration").item(0);
            narration = narrationElement.getTextContent();
            
            Action current = new Action(id, subjects, consumed, produced,
                narration);
            
            Element triggersElement =
                (Element)currentAction.getElementsByTagName(
                "triggers").item(0);
            NodeList triggers = triggersElement.getElementsByTagName(
                "keyphrase");
            for (int i = 0; i < triggers.getLength(); i++) {
                Element triggerElement = (Element)triggers.item(i);
                String trigger = triggerElement.getTextContent();
                if (trigger.indexOf(' ') == -1) {
                    if (actionsHashMap.containsKey(trigger)) {
                        actionsHashMap.get(trigger).add(current);
                    } else {
                        HashSet<Action> hs = new HashSet<>();
                        hs.add(current);
                        actionsHashMap.put(trigger, hs);
                    }
                } else {
                    boolean exists = false;
                    for (int j = 0; j < multiTriggerActions.size(); j++) {
                        if (multiTriggerActions.get(j).getTrigger().equals(trigger)) {
                            multiTriggerActions.get(j).addAction(current);
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
            currentAction = (Element)currentAction.getNextSibling();
        }
        return actionsHashMap;
    }
    
    
    private void readEntitiesFile(File file) throws FileNotFoundException, ParseException {
        Parser parser = new Parser();
        FileReader reader = new FileReader(file);
        parser.parse(reader);
        ArrayList<Graph> graphs = parser.getGraphs();
        
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
            
            for (int j = 0; j < contents.size(); j++) {
                String type = contents.get(j).getId().getId();
                if (type.equals("artefacts")) {
                    ArrayList<Node> artefactNodes =
                        contents.get(j).getNodes(false);
                    for (int k =  0; k < artefactNodes.size(); k++) {
                        Artefact a =
                            new Artefact(artefactNodes.get(k).getId().getId(),
                                artefactNodes.get(k).getAttribute("description"));
                        l.addArtefact(a);
                    }
                } else if (type.equals("furniture")) {
                    ArrayList<Node> furnitureNodes =
                        contents.get(j).getNodes(false);
                    for (int k =  0; k < furnitureNodes.size(); k++) {
                        Furniture f =
                            new Furniture(furnitureNodes.get(k).getId().getId(),
                                furnitureNodes.get(k).getAttribute(
                                    "description"));
                        l.addFurniture(f);
                    }
                } else if (type.equals("characters")) {
                    ArrayList<Node> characterNodes =
                        contents.get(j).getNodes(false);
                    for (int k =  0; k < characterNodes.size(); k++) {
                        GameCharacter c =
                            new GameCharacter(characterNodes.get(k).getId().getId(),
                                characterNodes.get(k).getAttribute(
                                    "description"));
                        l.addCharacter(c);
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
        for (int i = 0; i < pathEdges.size(); i++) {
            String startName =
                pathEdges.get(i).getSource().getNode().getId().getId();
            String endName =
                pathEdges.get(i).getTarget().getNode().getId().getId();
            Location start = null;
            Location end = null;
            for (int j = 0; j < locations.size(); j++) {
                if (locations.get(j).getName().equals(startName)) {
                    start = locations.get(j);
                } else if (locations.get(j).getName().equals(endName)) {
                    end = locations.get(j);
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
        for (Location l : locations) {
            ArrayList<GameCharacter> characters = l.getCharacters();
            for (GameCharacter c : characters) {
                if (c.getName().equals(userName)) {
                    p = (Player)c;
                }
            }
        }
        if (p == null) {
            p = new Player(userName);
            startLocation.addCharacter(p);
        }
        
        Location playerLocation = null;
        for (Location l : locations) {
            if (l.characterIsPresent(p)) {
                playerLocation = l;
            }
        }
        if (playerLocation == null) {
            throw new IOException("ERROR: player not in the game");
        }
        
        return handleInstruction(instruction, p, playerLocation);
    }
    
    private String handleInstruction(String inst, Player player,
                                     Location playerLocation) throws IOException {
        String[] words = inst.toLowerCase().split(" ");
        // TODO need to make sure this handles situations where more than one
        //  outcome is possible!! (i.e., ambiguous commands)
        for (String w : words) {
            if (w.equals("inventory") || w.equals("inv")) {
                return player.listItems();
            } else if (w.equals("get")) {
                return getArtefactFromLocation(words, player, playerLocation);
            } else if (w.equals("drop")) {
                return player.dropItem(words, playerLocation);
            } else if (w.equals("goto")) {
                return gotoLocation(words, player, playerLocation);
            } else if (w.equals("look")) {
                return playerLocation.lookAround(player);
            } else {
                if (singleTriggerActions.containsKey(w)) {
                    for (Action a : singleTriggerActions.get(w)) {
                        String output = tryAction(a, words, player,
                            playerLocation);
                        if (output.length() != 0) {
                            return output;
                        }
                    }
                }
            }
        }
        
        for (ActionTuple tup : multiTriggerActions) {
            // TODO handle multi-word triggers
        }
        
        return "unable to process that command.  Please try again\n";
    }
    
    /*
    *
"inventory" (or "inv" for short): lists all of the artefacts currently being carried by the player
"get": picks up a specified artefact from the current location and adds it into player's inventory
"drop": puts down an artefact from player's inventory and places it into the current location
"goto": moves the player to the specified location (if there is a path to that location)
"look": prints names and descriptions of entities in the current location and lists paths to other locations
    * */
    
    private String getArtefactFromLocation(String[] words, Player p,
                                           Location l) {
        for (String w : words) {
            Artefact a = l.removeArtefact(w);
            p.pickUpItem(a);
            return (p.getName() + " picked up " + a.getName() + "\n");
        }
        return (p.getName() + " is not holding any such item\n");
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
        return (p.getName() + " could not go to any of these locations as not" +
            " valid path exists\n");
    }
    
    private String tryAction(Action a, String[] words, Player p, Location l) throws IOException {
        boolean present = false;
        for (String subj : a.getSubjects()) {
            for (String word : words) {
                if (subj.equals(word)) {
                    present = true;
                }
            }
        }
        if (!present) {
            return "";
        }
        
        for (String s : a.getConsumed()) {
            if (!p.itemHeld(s) && !l.artefactIsPresent(s) && !l.furnitureIsPresent(s)) {
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
            }
        }
        
        for (String s : a.getProduced()) {
            l.produce(s, locations);
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
