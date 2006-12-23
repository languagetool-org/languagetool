/*
 * Created on 21.12.2006
 */
package de.danielnaber.languagetool.dev;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tools.Tools;

/**
 * Check texts from Wikipedia (http://download.wikimedia.org/backup-index.html).
 * 
 * @author Daniel Naber
 */
public class CheckWikipediaDump {

  private CheckWikipediaDump() {
    // no public constructor
  }
  
  /**
   * @param args
   * @throws IOException 
   * @throws ParserConfigurationException 
   * @throws SAXException 
   */
  public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
    CheckWikipediaDump prg = new CheckWikipediaDump();
    if (args.length != 1) {
      System.err.println("Usage: CheckWikipediaDump <filename>");
      System.exit(1);
    }
    prg.run(args[0]);
  }
  
  private void run(String filename) throws IOException, SAXException, ParserConfigurationException {
    JLanguageTool lt = new JLanguageTool(Language.GERMAN);
    lt.activateDefaultPatternRules();
    lt.disableRule("DE_CASE");    // too many false hits
    lt.disableRule("UPPERCASE_SENTENCE_START");    // TODO
    WikiDumpHandler handler = new WikiDumpHandler(lt);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(filename, handler);
  }

}

class WikiDumpHandler extends DefaultHandler {
    
  private JLanguageTool lt;
  private boolean inText = false;
  private StringBuilder text = new StringBuilder();
  
  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  WikiDumpHandler(JLanguageTool lt) {
    this.lt = lt;
  }
  
  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
    if (qName.equals("text")) {
      inText = true;
    }
  }

  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("text")) {
      inText = false;
      //System.err.println(text.length() + " " + text.substring(0, Math.min(50, text.length())));
      String textToCheck = cleanup(text.toString());
      if (!textToCheck.contains("#REDIRECT")) {
        //System.err.println("#########################");
        //System.err.println(textToCheck);
        try {
          Tools.checkText(textToCheck, lt);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      text = new StringBuilder();
    }
  }

  public void characters(char buf[], int offset, int len) {
    String s = new String(buf, offset, len);
    if (inText) {
      text.append(s);
    }
  }

  private String cleanup(String s) {
    //[[Bild:Alkalimetalle.jpg|thumb|left|alle 5 stabilen Alkalimetalle]]
    s = s.replaceAll("(?s)\\[\\[Bild:.*?\\]\\]", "");
    s = s.replaceAll("(?s)\\[\\[.*?\\|(.*?)\\]\\]", "$1");
    // e.g. [[el:Άτομο]]:
    s = s.replaceAll("(?s)\\[\\[...?:.*?\\]", "");
    // e.g. [[Chromosom]]en:
    s = s.replaceAll("(?s)\\[\\[(.*?)\\]\\]", "$1");
    s = s.replaceAll("(?s)<math>.*?</math>", "X");
    s = s.replaceAll("(?s)<!---.*?--->", "");
    s = s.replaceAll("(?si)\\{\\{prettytable\\}\\}.*?\\}", "");
    s = s.replaceAll("(?si)\\{\\{Personendaten.*?\\}", "");
    // e.g. [http://www.klassikakzente.de/page_26289.jsp Arvo Pärt]:
    s = s.replaceAll("(?si)\\[http://.*?\\]", "Quelle");
    s = s.replaceAll("(Quelle\\s*)+", "Quelle ");
    s = s.replaceAll("(Quelle\\*\\s*)+", "Quelle ");
    s = s.replaceAll("(\\*Quelle\\s*)+", "Quelle ");
    s = s.replaceAll("(?si)\\{\\{.*?\\}\\}", "");
    s = s.replaceAll("(?si)\\[\\[.*?\\]\\]", "");
    s = s.replaceAll("\n\\* ", "\n");
    s = s.replaceAll("'''", "");
    s = s.replaceAll("''", "");
    s = s.replaceAll("<br\\s*/>", "\n");
    s = s.replaceAll("&nbsp;", " ");
    return s;
  }
  
}
