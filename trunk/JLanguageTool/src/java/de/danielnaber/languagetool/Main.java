/* JLanguageTool, a natural language style checker 
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * The command line tool to check plain text files.
 *
 * @author Daniel Naber
 */
class Main {

  private final static int CONTEXT_SIZE = 25;
  
  private JLanguageTool lt = null;
  private boolean verbose = false;
  private Language language = null;

  private Main(boolean verbose, Language language) throws IOException {
    this.verbose = verbose;
    this.language = language;
    lt = new JLanguageTool(language);
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
        run(files[i].getAbsolutePath());
      }
    }
  }

  /**
   * Loads filename, filter out XML and check the result. Note that the XML filtering
   * can lead to incorrect positions in the list of matching rules.
   * 
   * @param filename
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  private void run(String filename) throws IOException,
      ParserConfigurationException, SAXException {
    long startTime = System.currentTimeMillis();
    File defaultPatternFile = 
      new File(JLanguageTool.RULES_DIR +File.separator+ language.getShortName() 
          +File.separator+ JLanguageTool.PATTERN_FILE);
    List patternRules = new ArrayList();
    if (defaultPatternFile.exists()) {
      patternRules = lt.loadPatternRules(defaultPatternFile.getAbsolutePath());
    } else {
      System.err.println("Pattern file " + defaultPatternFile.getAbsolutePath() + " not found");
    }
    for (Iterator iter = patternRules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      lt.addRule(rule);
    }
    if (verbose)
      lt.setOutput(System.err);
    String fileContents = readFile(filename);
    fileContents = filterXML(fileContents);
    List ruleMatches = lt.check(fileContents);
    long startTimeMatching = System.currentTimeMillis();
    for (Iterator iter = ruleMatches.iterator(); iter.hasNext();) {
      RuleMatch match = (RuleMatch) iter.next();
      System.out.println("Line " + (match.getLine()+1) + ", column " + match.getColumn());
      String msg = match.getMessage();
      msg = msg.replaceAll("<i>", "'");
      msg = msg.replaceAll("</i>", "'");
      System.out.println("Message: " + msg);
      System.out.println(getContext(match.getFromPos(), match.getToPos(), fileContents));
      if (iter.hasNext())
        System.out.println();
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Time: " + (endTime-startTime) + "ms (including " +(endTime-startTimeMatching)+
        "ms for rule matching)");
  }
  
  private String filterXML(String s) {
    s = s.replaceAll("(?s)<!--.*?-->", " ");      // (?s) = DOTALL mode
    s = s.replaceAll("(?s)<.*?>", " ");
    return s;
  }

  private String readFile(String filename) throws IOException {
    FileReader fr = null;
    BufferedReader br = null;
    StringBuffer sb = new StringBuffer();
    try {
      fr = new FileReader(filename);
      br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }
    } finally {
      if (br != null) br.close();
      if (fr != null) fr.close();
    }
    return sb.toString();
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

  private static void exitWithUsageMessagee() {
    System.out.println("Usage: java de.danielnaber.languagetool.Main " +
            "[-r|--recursive] [-v|--verbose] [-l|--language] <file>");
    System.exit(1);
  }

  /**
   * Command line tool to check plain text files.
   */
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    if (args.length < 1 || args.length > 5) {
      exitWithUsageMessagee();
    }
    boolean verbose = false;
    boolean recursive = false;
    Language language = null;
    String filename = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-v") || args[i].equals("--verbose")) {
        verbose = true;
      } else if (args[i].equals("-r") || args[i].equals("--recursive")) {
        recursive = true;
      } else if (args[i].equals("-l") || args[i].equals("--language")) {
        String lang = args[++i];
        if (lang.equals("en")) {
          language = Language.ENGLISH;
        } else if (lang.equals("de")) {
          language = Language.GERMAN;
        } else {
          exitWithUsageMessagee();
        }
      } else {
        filename = args[i];
      }
    }
    if (filename == null) {
      exitWithUsageMessagee();
    }
    if (language == null) {
      System.err.println("No language specified, using English");
      language = Language.ENGLISH;
    }
    Main prg = new Main(verbose, language);
    if (recursive)
      prg.runRecursive(filename);
    else
      prg.run(filename);
  }

}
