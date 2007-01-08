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
import de.danielnaber.languagetool.TextFilter;
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
  
  public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
    CheckWikipediaDump prg = new CheckWikipediaDump();
    if (args.length < 2 || args.length > 3) {
      System.err.println("Usage: CheckWikipediaDump <language> <filename> [maxArticleCheck]");
      System.exit(1);
    }
    int maxArticles = 0;
    if (args.length == 3)
      maxArticles = Integer.parseInt(args[2]);
    prg.run(args[0], args[1], maxArticles);
  }
  
  private void run(String language, String filename, int maxArticles) throws IOException, SAXException, ParserConfigurationException {
    Language lang = Language.getLanguageForShortName(language);
    if (lang == null) {
      System.err.println("Language not supported: " + language);
      System.exit(1);
    }
    JLanguageTool lt = new JLanguageTool(lang,  Language.GERMAN);
    lt.activateDefaultPatternRules();
    /*
    // useful settings for German:
    lt.disableRule("DE_CASE");    // too many false hits
    lt.disableRule("UPPERCASE_SENTENCE_START");
    lt.disableRule("DE_AGREEMENT");
    lt.disableRule("WORD_REPEAT_RULE");
    lt.disableRule("COMMA_PARENTHESIS_WHITESPACE");*/
    /*
    List rules = lt.getAllRules();
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule element = (Rule) iter.next();
      lt.disableRule(element.getId());
    }
    lt.enableRule("DE_AGREEMENT");
    */
    System.err.println("These rules are disabled: " + lt.getDisabledRules());
    WikiDumpHandler handler = new WikiDumpHandler(lt, maxArticles);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(filename, handler);
  }

}

class WikiDumpHandler extends DefaultHandler {
   
  private JLanguageTool lt;
  private int ruleMatches = 0;
  private int articleCount = 0;
  private int maxArticles = 0;

  private boolean inText = false;
  private StringBuilder text = new StringBuilder();
  
  private TextFilter textFilter = new WikipediaTextFilter();
  
  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  WikiDumpHandler(JLanguageTool lt, int maxArticles) {
    this.lt = lt;
    this.maxArticles = maxArticles;
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
      String textToCheck = textFilter.filter(text.toString());
      if (!textToCheck.contains("#REDIRECT")) {
        //System.err.println("#########################");
        //System.err.println(textToCheck);
        try {
          articleCount++;
          if (maxArticles > 0 && articleCount >= maxArticles) {
            System.out.printf("Maximim number of articles reached. Found %d matches in %d articles\n",
                ruleMatches, articleCount);
            System.exit(0);
          }
          System.out.println("checking article " + articleCount);
          ruleMatches += Tools.checkText(textToCheck, lt);
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
  
}

class WikipediaTextFilter implements TextFilter {

  public String filter(String s) {
    //[[Bild:Alkalimetalle.jpg|thumb|left|alle 5 stabilen Alkalimetalle]]
    s = s.replaceAll("(?s)\\[\\[Bild:.*?\\]\\]", "");
    s = s.replaceAll("(?s)\\[\\[[^\\[]*?\\|(.*?)\\]\\]", "$1");
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
