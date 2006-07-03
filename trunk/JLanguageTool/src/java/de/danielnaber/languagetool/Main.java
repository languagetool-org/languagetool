/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package de.danielnaber.languagetool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * The command line tool to check plain text files.
 *
 * @author Daniel Naber
 */
class Main {

  private final static int CONTEXT_SIZE = 25;

  private JLanguageTool lt = null;
  private boolean verbose = false;

  Main(boolean verbose, Language language, Language motherTongue) throws IOException, 
      ParserConfigurationException, SAXException {
    this(verbose, language, motherTongue, new String[0]);
  }
  
  Main(boolean verbose, Language language, Language motherTongue, String[] disabledRules) throws IOException, 
      SAXException, ParserConfigurationException {
    this.verbose = verbose;
    lt = new JLanguageTool(language, motherTongue);
    lt.activateDefaultPatternRules();
    lt.activateDefaultFalseFriendRules();
    for (int i = 0; i < disabledRules.length; i++) {
      lt.disableRule(disabledRules[i]);
    }
  }
  
  JLanguageTool getJLanguageTool() {
    return lt;
  }

  private void runRecursive(String filename) throws IOException,
      ParserConfigurationException, SAXException {
    File dir = new File(filename);
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory, cannot use recursion");
    }
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        runRecursive(files[i].getAbsolutePath());
      } else {
        String text = getFilteredText(files[i].getAbsolutePath());
        checkText(text);
      }
    }
  }

  /**
   * Loads filename, filter out XML and check the result. Note that the XML filtering
   * can lead to incorrect positions in the list of matching rules.
   * 
   * @param filename
   * @throws IOException
   */
  private String getFilteredText(String filename) throws IOException {
    if (verbose)
      lt.setOutput(System.err);
    System.out.println("Working on " + filename + "...");
    String fileContents = StringTools.readFile(filename);
    return filterXML(fileContents);
  }
  
  void checkText(String contents) throws IOException {
    long startTime = System.currentTimeMillis();
    List ruleMatches = lt.check(contents);
    long startTimeMatching = System.currentTimeMillis();
    int i = 1;
    for (Iterator iter = ruleMatches.iterator(); iter.hasNext();) {
      RuleMatch match = (RuleMatch) iter.next();
      System.out.println(i + ".) Line " + (match.getLine()+1) + ", column " + match.getColumn() +
          ", Rule ID: " + match.getRule().getId());
      String msg = match.getMessage();
      msg = msg.replaceAll("<em>", "'");
      msg = msg.replaceAll("</em>", "'");
      System.out.println("Message: " + msg);
      System.out.println(getContext(match.getFromPos(), match.getToPos(), contents));
      if (iter.hasNext())
        System.out.println();
      i++;
    }
    long endTime = System.currentTimeMillis();
    long time = endTime - startTime;
    float timeInSeconds = (float)time/1000.0f;
    float sentencesPerSecond = (float)lt.getSentenceCount() / (float)timeInSeconds;
    System.out.println("Time: " + time + "ms (including " +(endTime-startTimeMatching)+
        "ms for rule matching) for " + lt.getSentenceCount() + " sentences ("
        + sentencesPerSecond + " sentences/sec)");
  }
  
  private String filterXML(String s) {
    Pattern pattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(s);
    s = matcher.replaceAll(" ");
    pattern = Pattern.compile("<.*?>", Pattern.DOTALL);
    matcher = pattern.matcher(s);
    s = matcher.replaceAll(" ");
    return s;
  }

  private String getContext(int fromPos, int toPos, String fileContents) {
    fileContents = fileContents.replaceAll("\n", " ");
    // calculate context region:
    int startContent = fromPos - CONTEXT_SIZE;
    String prefix = "...";
    String postfix = "...";
    String markerPrefix = "   ";
    if (startContent < 0) {
      prefix = "";
      markerPrefix = "";
      startContent = 0;
    }
    int endContent = toPos + CONTEXT_SIZE;
    if (endContent > fileContents.length()) {
      postfix = "";
      endContent = fileContents.length();
    }
    // make "^" marker. inefficient but robust implementation:
    StringBuffer marker = new StringBuffer();
    for (int i = 0; i < fileContents.length() + prefix.length(); i++) {
      if (i >= fromPos && i < toPos)
        marker.append("^");
      else
        marker.append(" ");
    }
    // now build context string plus marker:
    StringBuffer sb = new StringBuffer();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    sb.append(postfix);
    sb.append("\n");
    sb.append(markerPrefix + marker.substring(startContent, endContent));
    return sb.toString();
  }

  private static void exitWithUsageMessage() {
    System.out.println("Usage: java de.danielnaber.languagetool.Main " +
            "[-r|--recursive] [-v|--verbose] [-l|--language LANG] [-m|--mothertongue LANG] [-d|--disable RULES] <file>");
    System.exit(1);
  }

  /**
   * Command line tool to check plain text files.
   */
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    if (args.length < 1 || args.length > 6) {
      exitWithUsageMessage();
    }
    boolean verbose = false;
    boolean recursive = false;
    Language language = null;
    Language motherTongue = null;
    String filename = null;
    String[] disabledRules = new String[0];
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-h") || args[i].equals("-help") || args[i].equals("--help")) {
        exitWithUsageMessage();
      } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
        verbose = true;
      } else if (args[i].equals("-r") || args[i].equals("--recursive")) {
        recursive = true;
      } else if (args[i].equals("-d") || args[i].equals("--disable")) {
        String rules = args[++i];
        disabledRules = rules.split(",");
      } else if (args[i].equals("-l") || args[i].equals("--language")) {
        language = getLanguageOrExit(args[++i]);
      } else if (args[i].equals("-m") || args[i].equals("--mothertongue")) {
        motherTongue = getLanguageOrExit(args[++i]);
      } else {
        filename = args[i];
      }
    }
    if (filename == null) {
      exitWithUsageMessage();
    }
    if (language == null) {
      System.err.println("No language specified, using English");
      language = Language.ENGLISH;
    }
    Main prg = new Main(verbose, language, motherTongue, disabledRules);
    if (recursive) {
      prg.runRecursive(filename);
    } else {
      String text = prg.getFilteredText(filename);
      prg.checkText(text);
    }
  }

  private static Language getLanguageOrExit(String lang) {
    Language language = null;
    boolean foundLanguage = false;
    List<String> supportedLanguages = new ArrayList<String>();
    for (int j = 0; j < Language.LANGUAGES.length; j++) {
      Language tmpLang = Language.LANGUAGES[j];
      supportedLanguages.add(tmpLang.getShortName());
      if (lang.equals(tmpLang.getShortName())) {
        language = tmpLang;
        foundLanguage = true;
        break;
      }          
    }
    if (! foundLanguage) {
      System.out.println("Unknown language '" + lang + "'. Supported languages are: " + supportedLanguages);
      exitWithUsageMessage();
    }
    return language;
  }

}
