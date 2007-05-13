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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.tools.StringTools;
import de.danielnaber.languagetool.tools.Tools;

/**
 * The command line tool to check plain text files.
 *
 * @author Daniel Naber
 */
class Main {

  private JLanguageTool lt = null;
  private boolean verbose = false;
  private boolean apiFormat = false;

  Main(boolean verbose, Language language, Language motherTongue) throws IOException, 
      ParserConfigurationException, SAXException {
    this(verbose, language, motherTongue, new String[0], new String[0]);
  }

  Main(boolean verbose, Language language, Language motherTongue, String[] disabledRules,
      String[] enabledRules) throws IOException, SAXException, ParserConfigurationException {
      this(verbose, language, motherTongue, disabledRules, enabledRules, false);
  }

  Main(boolean verbose, Language language, Language motherTongue, String[] disabledRules,
      String[] enabledRules, boolean apiFormat) throws IOException, 
      SAXException, ParserConfigurationException {
    this.verbose = verbose;
    this.apiFormat = apiFormat;
    lt = new JLanguageTool(language, motherTongue);
    lt.activateDefaultPatternRules();
    lt.activateDefaultFalseFriendRules();
    // disable rules that are disabled explicitly:
    for (int i = 0; i < disabledRules.length; i++) {
      lt.disableRule(disabledRules[i]);
    }
    // disable all rules except those enabled explictly, if any:
    if (enabledRules.length > 0) {
      Set<String> enabledRuleIDs = new HashSet<String>(Arrays.asList(enabledRules));
      for (Rule rule : lt.getAllRules()) {
        if (!enabledRuleIDs.contains(rule.getId())) {
          lt.disableRule(rule.getId());
        }
      }
    }
  }
  
  private void setListUnknownWords(boolean listUnknownWords) {
    lt.setListUnknownWords(listUnknownWords);
  }

  JLanguageTool getJLanguageTool() {
    return lt;
  }

  private void runRecursive(final String filename, final String encoding) throws IOException,
      ParserConfigurationException, SAXException {
    File dir = new File(filename);
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory, cannot use recursion");
    }
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        runRecursive(files[i].getAbsolutePath(), encoding);
      } else {
        String text = getFilteredText(files[i].getAbsolutePath(), encoding);
        Tools.checkText(text, lt);
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
  private String getFilteredText(final String filename, final String encoding) throws IOException {
    if (verbose)
      lt.setOutput(System.err);
    if (!apiFormat)
      System.out.println("Working on " + filename + "...");
    String fileContents = StringTools.readFile(new FileInputStream(filename), encoding);
    return filterXML(fileContents);
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

  private static void exitWithUsageMessage() {
    System.out.println("Usage: java de.danielnaber.languagetool.Main " +
            "[-r|--recursive] [-v|--verbose] [-l|--language LANG] [-m|--mothertongue LANG] [-d|--disable RULES] " +
            "[-e|--enable RULES] [-c|--encoding] [-u|--list-unknown] [-b] <file>");
    System.exit(1);
  }

  /**
   * Command line tool to check plain text files.
   */
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    if (args.length < 1 || args.length > 9) {
      exitWithUsageMessage();
    }
    boolean verbose = false;
    boolean recursive = false;
    boolean singleLineBreakMarksParagraph = false;
    boolean apiFormat = false;
    boolean listUnknown = false;
    Language language = null;
    Language motherTongue = null;
    String encoding = null;
    String filename = null;
    String[] disabledRules = new String[0];
    String[] enabledRules = new String[0];
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-h") || args[i].equals("-help") || args[i].equals("--help")) {
        exitWithUsageMessage();
      } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
        verbose = true;
      } else if (args[i].equals("-r") || args[i].equals("--recursive")) {
        recursive = true;
      } else if (args[i].equals("-d") || args[i].equals("--disable")) {
        if (enabledRules.length > 0)
          throw new IllegalArgumentException("You cannot specifiy both enabled and disabled rules");
        String rules = args[++i];
        disabledRules = rules.split(",");
      } else if (args[i].equals("-e") || args[i].equals("--enable")) {
        if (disabledRules.length > 0)
          throw new IllegalArgumentException("You cannot specifiy both enabled and disabled rules");
        String rules = args[++i];
        enabledRules = rules.split(",");
      } else if (args[i].equals("-l") || args[i].equals("--language")) {
        language = getLanguageOrExit(args[++i]);
      } else if (args[i].equals("-m") || args[i].equals("--mothertongue")) {
        motherTongue = getLanguageOrExit(args[++i]);
      } else if (args[i].equals("-c") || args[i].equals("--encoding")) {
        encoding = args[++i];
      } else if (args[i].equals("-u") || args[i].equals("--list-unknown")) {
        listUnknown = true;
      } else if (args[i].equals("-b")) {
        singleLineBreakMarksParagraph = true;
      } else if (i == args.length - 1) {
        filename = args[i];
      } else if (args[i].equals("--api")) {
        apiFormat = true;
      } else {
        System.err.println("Unknown option: " + args[i]);
        exitWithUsageMessage();
      }
    }
    if (filename == null) {
      exitWithUsageMessage();
    }
    if (language == null) {
      if (!apiFormat)
        System.err.println("No language specified, using English");
      language = Language.ENGLISH;
    } else if (!apiFormat) {
      System.out.println("Expected text language: " + language.getName());
    }
    language.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(singleLineBreakMarksParagraph);
    Main prg = new Main(verbose, language, motherTongue, disabledRules, enabledRules, apiFormat);
    prg.setListUnknownWords(listUnknown);
    if (recursive) {
      prg.runRecursive(filename, encoding);
    } else {
      String text = prg.getFilteredText(filename, encoding);
      Tools.checkText(text, prg.getJLanguageTool(), apiFormat);
      if (listUnknown) {
        System.out.println("Unknown words: " + prg.getJLanguageTool().getUnknownWords());
      }
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
    if (!foundLanguage) {
      System.out.println("Unknown language '" + lang + "'. Supported languages are: " + supportedLanguages);
      exitWithUsageMessage();
    }
    return language;
  }

}
