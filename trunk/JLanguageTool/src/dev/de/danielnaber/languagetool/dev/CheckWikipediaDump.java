/*
 * Created on 21.12.2006
 */
package de.danielnaber.languagetool.dev;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TextFilter;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

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
    File file = new File(filename);
    if (!file.exists() || !file.isFile()) {
      throw new IOException("File doesn't exist or isn't a file: " + filename);
    }
    Language lang = Language.getLanguageForShortName(language);
    if (lang == null) {
      System.err.println("Language not supported: " + language);
      System.exit(1);
    }
    JLanguageTool lt = new JLanguageTool(lang);
    lt.activateDefaultPatternRules();
    // useful settings (avoid false alarms) because text extraction
    // from Wikipedia isn't clean yet:
    lt.disableRule("DE_CASE");    // too many false hits
    lt.disableRule("UNPAIRED_BRACKETS");
    lt.disableRule("UPPERCASE_SENTENCE_START");
    lt.disableRule("WORD_REPEAT_RULE");
    lt.disableRule("COMMA_PARENTHESIS_WHITESPACE");
    lt.disableRule("WHITESPACE_RULE");
    /*
    List rules = lt.getAllRules();
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule element = (Rule) iter.next();
      lt.disableRule(element.getId());
    }
    lt.enableRule("DE_AGREEMENT");
    */
    System.err.println("These rules are disabled: " + lt.getDisabledRules());
    Date dumpDate = getDumpDate(file);
    String dumpLangCode = getDumpLanguageCode(file);
    System.out.println("Dump date: " + dumpDate + ", language: " + dumpLangCode);
    WikiDumpHandler handler = new WikiDumpHandler(lt, maxArticles, dumpDate, dumpLangCode);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(file, handler);
  }

  private Date getDumpDate(File file) throws IOException {
    String filename = file.getName();
    String[] parts = filename.split("-");
    if (parts.length < 3) {
      throw new IOException("Unexpected filename format: " + file.getName());
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    try {
      return sdf.parse(parts[1]);
    } catch (ParseException e) {
      throw new IOException("Unexpected date format: " + parts[1], e);
    }
  }

  private String getDumpLanguageCode(File file) throws IOException {
    String filename = file.getName();
    int wikiPos = filename.indexOf("wiki");
    if (wikiPos == -1) {
      throw new IOException("Unexpected filename format, no 'xxwiki': " + file.getName());
    }
    return filename.substring(0, wikiPos);
  }

}

class WikiDumpHandler extends DefaultHandler {

  //FIXME: make configurable
  private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
  private static final String DB_URL = "jdbc:mysql://localhost/ltcommunity";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "";

  private static final int CONTEXT_SIZE = 50;
  private static final String MARKER_START = "<err>";
  private static final String MARKER_END = "</err>";
  private static final String URL_PREFIX = "http://XX.wikipedia.org/wiki/";
  
  private JLanguageTool lt;
  private int ruleMatchCount = 0;
  private int articleCount = 0;
  private int maxArticles = 0;

  private boolean inText = false;
  private StringBuilder text = new StringBuilder();
  
  private TextFilter textFilter = new WikipediaTextFilter();

  private Connection conn;
  private Date dumpDate;
  private String langCode;
  private String title;

  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  WikiDumpHandler(JLanguageTool lt, int maxArticles, Date dumpDate, String langCode) {
    this.lt = lt;
    this.maxArticles = maxArticles;
    this.dumpDate = dumpDate;
    this.langCode = langCode;
    try {
      Class.forName(DB_DRIVER);
      conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName,
      Attributes attrs) throws SAXException {
    if (qName.equals("title")) {
      inText = true;
    } else if (qName.equals("text")) {
      inText = true;
    }
  }

  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("title")) {
      title = text.toString();
    } else if (qName.equals("text")) {
      inText = false;
      //System.err.println(text.length() + " " + text.substring(0, Math.min(50, text.length())));
      String textToCheck = textFilter.filter(text.toString());
      //System.out.println(textToCheck);
      if (!textToCheck.contains("#REDIRECT")) {
        //System.err.println("#########################");
        //System.err.println(textToCheck);
        try {
          articleCount++;
          if (maxArticles > 0 && articleCount > maxArticles) {
            System.out.printf("Maximim number of articles reached. Found %d matches in %d articles\n",
                ruleMatchCount, articleCount);
            System.exit(0);
          }
          List<RuleMatch> ruleMatches = lt.check(textToCheck);  
          System.out.println("Checking article " + articleCount + " (" + title + ")" + 
              ", found " + ruleMatches.size() + " matches");
          saveResultToDatabase(ruleMatches, textToCheck, lt.getLanguage());
          ruleMatchCount += ruleMatches.size();
        } catch (IOException e) {
          throw new RuntimeException(e);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
      text = new StringBuilder();
    }
  }

  private void saveResultToDatabase(List<RuleMatch> ruleMatches,
      String text, Language language) throws SQLException {
    String sql = "INSERT INTO CorpusMatch " +
    		"(languageCode, ruleID, message, errorContext, corpusDate, checkDate, sourceUri) "+
    		"VALUES (?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement prepSt = conn.prepareStatement(sql);
    for (RuleMatch match : ruleMatches) {
      prepSt.setString(1, language.getShortName());
      prepSt.setString(2, match.getRule().getId());
      prepSt.setString(3, match.getMessage());
      prepSt.setString(4, Tools.getContext(match.getFromPos(),
            match.getToPos(), text, CONTEXT_SIZE, MARKER_START, MARKER_END));
      prepSt.setDate(5, new java.sql.Date(dumpDate.getTime()));
      prepSt.setDate(6, new java.sql.Date(new Date().getTime()));
      prepSt.setString(7, URL_PREFIX.replaceAll("XX", langCode) + title);
      prepSt.executeUpdate();
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
    s = s.replaceAll("(?s)\\[...?:.*?\\]\\]", "");
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
    s = s.replaceAll("\n+", "\n");
    s = s.replaceAll("&nbsp;", " ");
    return s;
  }

}
