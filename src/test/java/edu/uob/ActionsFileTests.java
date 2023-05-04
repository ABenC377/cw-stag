package edu.uob;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for making sure action files are read in correctly
 */
final class ActionsFileTests {

  // Test to make sure that the basic actions file is readable
  @Test
  void testBasicActionsFileIsReadable() {
      try {
          final DocumentBuilder builder =
              DocumentBuilderFactory.newInstance().newDocumentBuilder();
          final Document document = builder.parse("config" + File.separator +
              "basic-actions.xml");
          final Element root = document.getDocumentElement();
          final NodeList actions = root.getChildNodes();
          // Get the first action (only the odd items are actually actions - 1, 3, 5 etc.)
          final Element firstAction = (Element)actions.item(1);
          final Element triggers = (Element)firstAction.getElementsByTagName(
              "triggers").item(0);
          // Get the first trigger phrase
          final String firstTrigger = triggers.getElementsByTagName(
              "keyphrase").item(0).getTextContent();
          assertEquals("open", firstTrigger, "First trigger phrase was not 'open'");
      } catch(ParserConfigurationException pce) {
          fail("ParserConfigurationException was thrown when attempting to read basic actions file");
      } catch(SAXException saxe) {
          fail("SAXException was thrown when attempting to read basic actions file");
      } catch(IOException ioe) {
          fail("IOException was thrown when attempting to read basic actions file");
      }
  }

}
