package edu.uob;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class EntitiesFileTests {

  // Test to make sure that the basic entities file is readable
  @Test
  void testBasicEntitiesFileIsReadable() {
      try {
          final Parser parser = new Parser();
          final FileReader reader = new FileReader("config" + File.separator + "basic-entities.dot");
          parser.parse(reader);
          final Graph wholeDocument = parser.getGraphs().get(0);
          final ArrayList<Graph> sections = wholeDocument.getSubgraphs();

          // The locations will always be in the first subgraph
          final ArrayList<Graph> locations = sections.get(0).getSubgraphs();
          final Graph firstLocation = locations.get(0);
          final Node locationDetails = firstLocation.getNodes(false).get(0);
          // Yes, you do need to get the ID twice !
          final String locationName = locationDetails.getId().getId();
          assertEquals("cabin", locationName, "First location should have been 'cabin'");

          // The paths will always be in the second subgraph
          final ArrayList<Edge> paths = sections.get(1).getEdges();
          final Edge firstPath = paths.get(0);
          final Node fromLocation = firstPath.getSource().getNode();
          final String fromName = fromLocation.getId().getId();
          final Node toLocation = firstPath.getTarget().getNode();
          final String toName = toLocation.getId().getId();
          assertEquals("cabin", fromName, "First path should have been from 'cabin'");
          assertEquals("forest", toName, "First path should have been to 'forest'");

      } catch (FileNotFoundException fnfe) {
          fail("FileNotFoundException was thrown when attempting to read basic entities file");
      } catch (ParseException pe) {
          fail("ParseException was thrown when attempting to read basic entities file");
      }
  }

}
