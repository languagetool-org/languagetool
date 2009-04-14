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

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

  private JLanguageTool lt;
  private boolean verbose;
  private boolean apiFormat;
  private boolean taggerOnly;

  /* maximum file size to read in a single read */
  private static final int MAXFILESIZE = 64000;

  Main(final boolean verbose, final Language language, final Language motherTongue)
  throws IOException, ParserConfigurationException, SAXException {
    this(verbose, language, motherTongue, new String[0], new String[0]);
  }

  Main(final boolean verbose, final Language language, final Language motherTongue,
      final String[] disabledRules, final String[] enabledRules) throws IOException,
      SAXException, ParserConfigurationException {
    this(verbose, false, language, motherTongue, disabledRules, enabledRules,
        false);
  }

  Main(final boolean verbose, final boolean taggerOnly, final Language language,
      final Language motherTongue, final String[] disabledRules, final String[] enabledRules,
      final boolean apiFormat) throws IOException, SAXException,
      ParserConfigurationException {
    this.verbose = verbose;
    this.apiFormat = apiFormat;
    this.taggerOnly = taggerOnly;
    lt = new JLanguageTool(language, motherTongue);
    lt.activateDefaultPatternRules();
    lt.activateDefaultFalseFriendRules();
    // disable rules that are disabled explicitly:
    for (final String disabledRule: disabledRules) {
      lt.disableRule(disabledRule);
    }
    // disable all rules except those enabled explicitly, if any:
    if (enabledRules.length > 0) {
      final Set<String> enabledRuleIDs = new HashSet<String>(Arrays
          .asList(enabledRules));
      for (String ruleName: enabledRuleIDs) {
        lt.enableDefaultOffRule(ruleName);
        lt.enableRule(ruleName);
      }
      for (Rule rule : lt.getAllRules()) {
        if (!enabledRuleIDs.contains(rule.getId())) {
          lt.disableRule(rule.getId());
        }
      }
    }
  }

  private void setListUnknownWords(final boolean listUnknownWords) {
    lt.setListUnknownWords(listUnknownWords);
  }

  JLanguageTool getJLanguageTool() {
    return lt;
  }

  private void runOnFile(final String filename, final String encoding,
      final boolean listUnknownWords) throws IOException {
    boolean oneTime = false;
    if (!"-".equals(filename)) {
      final File file = new File(filename);
      oneTime = file.length() < MAXFILESIZE;
    }    
    if (oneTime) {
      final String text = getFilteredText(filename, encoding);
      if (!taggerOnly) {
        Tools.checkText(text, lt, apiFormat, 0);
      } else {
        Tools.tagText(text, lt);
      }
      if (listUnknownWords) {
        System.out.println("Unknown words: " + lt.getUnknownWords());
      }
    } else {
      if (verbose) {
        lt.setOutput(System.err);
      }
      if (!apiFormat) {
        if (!"-".equals(filename)) {
          System.out.println("Working on " + filename
              + "... in a line mode");
        } else {
          System.out.println("Working on STDIN in a line mode.");
        }
      }
      InputStreamReader isr = null;
      BufferedReader br = null;      
      int lineOffset = 0;
      int matches = 0;
      long sentences = 0;
      List<String> unknownWords = new ArrayList<String>();
      StringBuffer sb = new StringBuffer();
      final long startTime = System.currentTimeMillis();
      try {
        if (!"-".equals(filename)) {
          final File file = new File(filename);
          if (encoding != null) {
            isr = new InputStreamReader(new BufferedInputStream(
                new FileInputStream(file.getAbsolutePath())), encoding);
          } else {
            isr = new InputStreamReader(new BufferedInputStream(
                new FileInputStream(file.getAbsolutePath())));
          } 
        } else {
          if (encoding != null) {
            isr = new InputStreamReader(new BufferedInputStream(
                System.in), encoding);
          } else {
            isr = new InputStreamReader(new BufferedInputStream(
                System.in));
          }
        }
        br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
          sb.append("\n");
          if (!taggerOnly) {
            if (matches == 0) {
              matches += Tools.checkText(StringTools.filterXML(sb.toString()), lt, apiFormat, -1, lineOffset, matches, StringTools.XmlPrintMode.START_XML);                
            } else {
              matches += Tools.checkText(StringTools.filterXML(sb.toString()), lt, apiFormat, -1, lineOffset, matches, StringTools.XmlPrintMode.CONTINUE_XML);  
            }
            sentences += lt.getSentenceCount();
          } else {
            Tools.tagText(StringTools.filterXML(sb.toString()), lt);
          }
          if (listUnknownWords && !taggerOnly) {
            for (String word : lt.getUnknownWords())
              if (!unknownWords.contains(word)) {
                unknownWords.add(word);
              }                            
          }
          sb = new StringBuffer();
          lineOffset++;
        }
      } finally {

        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        final float timeInSeconds = time / 1000.0f;
        final float sentencesPerSecond = sentences / timeInSeconds;
        if (apiFormat) {
          System.out.println("<!--");
        }
        System.out.printf(Locale.ENGLISH,
            "Time: %dms for %d sentences (%.1f sentences/sec)", time, sentences, sentencesPerSecond);
        System.out.println();        
        Collections.sort(unknownWords);
        System.out.println("Unknown words: " + unknownWords);
        if (apiFormat) {
          System.out.println("-->");
        }        

        if (br != null) {
          br.close();
        }
        if (isr != null) {
          isr.close();
        }
      }
    }
  }

  private void runRecursive(final String filename, final String encoding,
      final boolean listUnknown) throws IOException, ParserConfigurationException,
      SAXException {
    final File dir = new File(filename);
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir.getAbsolutePath()
          + " is not a directory, cannot use recursion");
    }
    final File[] files = dir.listFiles();
    for (final File file: files) {
      if (file.isDirectory()) {
        runRecursive(file.getAbsolutePath(), encoding, listUnknown);
      } else {
        runOnFile(file.getAbsolutePath(), encoding, listUnknown);
      }
    }
  }

  /**
   * Loads filename, filter out XML and check the result. Note that the XML
   * filtering can lead to incorrect positions in the list of matching rules.
   * 
   * @param filename
   * @throws IOException
   */
  private String getFilteredText(final String filename, final String encoding)
  throws IOException {
    if (verbose) {
      lt.setOutput(System.err);
    }
    if (!apiFormat) {
      System.out.println("Working on " + filename + "...");
    }
    final String fileContents = StringTools.readFile(new FileInputStream(filename),
        encoding);
    return StringTools.filterXML(fileContents);
  }



  private static void exitWithUsageMessage() {
    System.out
    .println("Usage: java de.danielnaber.languagetool.Main "
        + "[-r|--recursive] [-v|--verbose] [-l|--language LANG] [-m|--mothertongue LANG] [-d|--disable RULES] "
        + "[-e|--enable RULES] [-c|--encoding] [-u|--list-unknown] [-t|--taggeronly] [-b] [--api] <file>");
    System.exit(1);
  }

  /**
   * Command line tool to check plain text files.
   */
  public static void main(final String[] args) throws IOException,
  ParserConfigurationException, SAXException {
    if (args.length < 1 || args.length > 9) {
      exitWithUsageMessage();
    }
    boolean verbose = false;
    boolean recursive = false;
    boolean taggerOnly = false;
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
      if (args[i].equals("-h") || args[i].equals("-help")
          || args[i].equals("--help") || args[i].equals("--?")) {
        exitWithUsageMessage();
      } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
        verbose = true;
      } else if (args[i].equals("-t") || args[i].equals("--taggeronly")) {
        taggerOnly = true;
        if (listUnknown) {
          throw new IllegalArgumentException(
              "You cannot list unknown words when tagging only.");
        }
      } else if (args[i].equals("-r") || args[i].equals("--recursive")) {
        recursive = true;
      } else if (args[i].equals("-d") || args[i].equals("--disable")) {
        if (enabledRules.length > 0) {
          throw new IllegalArgumentException(
              "You cannot specify both enabled and disabled rules");
        }
        final String rules = args[++i];
        disabledRules = rules.split(",");
      } else if (args[i].equals("-e") || args[i].equals("--enable")) {
        if (disabledRules.length > 0) {
          throw new IllegalArgumentException(
              "You cannot specify both enabled and disabled rules");
        }
        final String rules = args[++i];
        enabledRules = rules.split(",");
      } else if (args[i].equals("-l") || args[i].equals("--language")) {
        language = getLanguageOrExit(args[++i]);
      } else if (args[i].equals("-m") || args[i].equals("--mothertongue")) {
        motherTongue = getLanguageOrExit(args[++i]);
      } else if (args[i].equals("-c") || args[i].equals("--encoding")) {
        encoding = args[++i];
      } else if (args[i].equals("-u") || args[i].equals("--list-unknown")) {
        listUnknown = true;
        if (taggerOnly) {
          throw new IllegalArgumentException(
              "You cannot list unknown words when tagging only.");
        }
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
      filename = "-";
    }
    if (language == null) {
      if (!apiFormat) {
        System.err.println("No language specified, using English");
      }
      language = Language.ENGLISH;
    } else if (!apiFormat) {
      System.out.println("Expected text language: " + language.getName());
    }
    language.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(
        singleLineBreakMarksParagraph);
    final Main prg = new Main(verbose, taggerOnly, language, motherTongue,
        disabledRules, enabledRules, apiFormat);
    prg.setListUnknownWords(listUnknown);
    if (recursive) {
      prg.runRecursive(filename, encoding, listUnknown);
    } else {
      /*
       * String text = prg.getFilteredText(filename, encoding);
       * Tools.checkText(text, prg.getJLanguageTool(), apiFormat);
       */
      prg.runOnFile(filename, encoding, listUnknown);
    }
  }

  private static Language getLanguageOrExit(final String lang) {
    Language language = null;
    boolean foundLanguage = false;
    final List<String> supportedLanguages = new ArrayList<String>();
    for (final Language tmpLang : Language.LANGUAGES) {      
      supportedLanguages.add(tmpLang.getShortName());
      if (lang.equals(tmpLang.getShortName())) {
        language = tmpLang;
        foundLanguage = true;
        break;
      }
    }
    if (!foundLanguage) {
      System.out.println("Unknown language '" + lang
          + "'. Supported languages are: " + supportedLanguages);
      exitWithUsageMessage();
    }
    return language;
  }

}
