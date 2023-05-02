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
    private final ArrayList<Location> initialLocations = new ArrayList<>();
    private final ArrayList<GameEntity> initialEntities = new ArrayList<>();
    private final CommandHandler handler;
    
    
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
        
        handler = new CommandHandler(startLocation, storeRoom,
            initialLocations, initialEntities, oneWordActions,
            manyWordActions);
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
            for (final Location l : initialLocations) {
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
        initialEntities.add(location);
        
        final ArrayList<Graph> contents = graph.getSubgraphs();
        
        for (final Graph content : contents) {
            addEntityToLocation(location, content);
        }
        
        initialLocations.add(location);
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
                    initialEntities.add(artefact);
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
                    initialEntities.add(furniture);
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
                    initialEntities.add(character);
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
        return handler.handle(command);
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
        try (ServerSocket socket = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(socket);
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
        try (Socket socket = serverSocket.accept();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer =
                 new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
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
