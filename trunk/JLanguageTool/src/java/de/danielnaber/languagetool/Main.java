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

import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.apache.tika.language.*;

import de.danielnaber.languagetool.bitext.TabBitextReader;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.bitext.BitextRule;
import de.danielnaber.languagetool.tools.StringTools;
import de.danielnaber.languagetool.tools.Tools;
import de.danielnaber.languagetool.tools.*;

/**
 * The command line tool to check plain text files.
 * 
 * @author Daniel Naber
 */
class Main {

  private JLanguageTool lt;
  private final boolean verbose;
  private final boolean apiFormat;
  private final boolean taggerOnly;
  private final boolean applySuggestions;
  private boolean profileRules;
  private boolean bitextMode;
  private boolean autoDetect;
  private boolean singleLineBreakMarksParagraph;
  private String[] enabledRules;
  private String[] disabledRules;
  private Language motherTongue;
  private JLanguageTool srcLt;
  List<BitextRule> bRules;
  private Rule currentRule;

  /* maximum file size to read in a single read */
  private static final int MAX_FILE_SIZE = 64000;

  Main(final boolean verbose, final boolean taggerOnly,
      final Language language, final Language motherTongue,
      final String[] disabledRules, final String[] enabledRules,
      final boolean apiFormat, boolean applySuggestions, 
      boolean autoDetect, boolean singleLineBreakMarksParagraph) throws IOException,
      SAXException, ParserConfigurationException {
    this.verbose = verbose;
    this.apiFormat = apiFormat;
    this.taggerOnly = taggerOnly;
    this.applySuggestions = applySuggestions;
    this.autoDetect = autoDetect;
    this.enabledRules = enabledRules;
    this.disabledRules = disabledRules;
    this.motherTongue = motherTongue;
    this.singleLineBreakMarksParagraph = singleLineBreakMarksParagraph;
    profileRules = false;
    bitextMode = false;
    srcLt = null;
    bRules = null;
    lt = new JLanguageTool(language, motherTongue);
    lt.activateDefaultPatternRules();
    lt.activateDefaultFalseFriendRules();
    selectRules(lt, disabledRules, enabledRules);
  }
  
  private void selectRules(final JLanguageTool lt, final String[] disabledRules, final String[] enabledRules) {
 // disable rules that are disabled explicitly:
    for (final String disabledRule : disabledRules) {
      lt.disableRule(disabledRule);
    }
    // disable all rules except those enabled explicitly, if any:
    if (enabledRules.length > 0) {
      final Set<String> enabledRuleIDs = new HashSet<String>(Arrays
          .asList(enabledRules));
      for (String ruleName : enabledRuleIDs) {
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

  private void setProfilingMode() {
    profileRules = true;
  }

  private void setBitextMode(final Language sourceLang,
      final String[] disabledRules, final String[] enabledRules) throws IOException, ParserConfigurationException, SAXException {
    bitextMode = true;
    final Language target = lt.getLanguage();
    lt = new JLanguageTool(target, null);    
    srcLt = new JLanguageTool(sourceLang);
    lt.activateDefaultPatternRules();
    selectRules(lt, disabledRules, enabledRules);
    selectRules(srcLt, disabledRules, enabledRules);
    bRules = Tools.getBitextRules(sourceLang, lt.getLanguage());

    List<BitextRule> bRuleList = new ArrayList<BitextRule>(bRules);    
    for (final BitextRule br : bRules) {
      for (final String disabledRule : disabledRules) {
        if (br.getId().equals(disabledRule)) {        
          bRuleList.remove(br);
        }
      }
    }
    bRules = bRuleList;
    if (enabledRules.length > 0) {
      bRuleList = new ArrayList<BitextRule>();
      for (final String enabledRule : enabledRules) {
        for (final BitextRule br : bRules) {
          if (br.getId().equals(enabledRule)) {
            bRuleList.add(br);
          }
        }
      }
      bRules = bRuleList;
    }
  }

  JLanguageTool getJLanguageTool() {
    return lt;
  }

  private void runOnFile(final String filename, final String encoding,
      final boolean listUnknownWords) throws IOException {
    boolean oneTime = false;
    if (!"-".equals(filename)) {
      if (autoDetect) {
          Language language = getLanguageFromFile(filename, encoding);
          if (language == null) {
              System.err.println("Could not detect language well enough, using English");
              language = Language.ENGLISH;
          }
          try {
              changeLanguage(language, motherTongue, disabledRules, enabledRules);
          } catch (SAXException e) {
              e.printStackTrace();
          } catch (ParserConfigurationException e) {
              e.printStackTrace();
          }
          System.out.println("Using " + language.getName() + " for file " + filename);
      }
      final File file = new File(filename);
      // run once on file if the file size < MAXFILESIZE or
      // when we use the bitext mode (we use a bitext reader
      // instead of a direct file access)
      oneTime = file.length() < MAX_FILE_SIZE || bitextMode;
    }
    if (oneTime) {
      runOnFileInOneGo(filename, encoding, listUnknownWords);
    } else {
      runOnFileLineByLine(filename, encoding, listUnknownWords);
    }
  }

  private void runOnFileInOneGo(String filename, String encoding, boolean listUnknownWords) throws IOException {
    if (bitextMode) {
      //TODO: add parameter to set different readers
      final TabBitextReader reader = new TabBitextReader(filename, encoding);
      if (applySuggestions) {
        Tools.correctBitext(reader, srcLt, lt, bRules);
      } else {
        Tools.checkBitext(reader, srcLt, lt, bRules,
          apiFormat);
      }
    } else {
      final String text = getFilteredText(filename, encoding);
      if (applySuggestions) {
        System.out.print(Tools.correctText(text, lt));
      } else if (profileRules) {
        Tools.profileRulesOnText(text, lt);
      } else if (!taggerOnly) {
        Tools.checkText(text, lt, apiFormat, 0);
      } else {
        Tools.tagText(text, lt);
      }
      if (listUnknownWords) {
        System.out.println("Unknown words: " + lt.getUnknownWords());
      }
    }
  }

  private void runOnFileLineByLine(String filename, String encoding, boolean listUnknownWords) throws IOException {
    if (verbose) {
      lt.setOutput(System.err);
    }
    if (!apiFormat && !applySuggestions) {
      if ("-".equals(filename)) {
        System.out.println("Working on STDIN...");
      } else {
        System.out.println("Working on " + filename + "...");
      }
    }
    int runCount = 1;
    final List<Rule> rules = lt.getAllRules();
    if (profileRules) {
      System.out.printf("Testing %d rules\n", rules.size());
      System.out.println("Rule ID\tTime\tSentences\tMatches\tSentences per sec.");
      runCount = rules.size();
    }
    InputStreamReader isr = null;
    BufferedReader br = null;
    int lineOffset = 0;
    int tmpLineOffset = 0;
    final List<String> unknownWords = new ArrayList<String>();
    StringBuilder sb = new StringBuilder();
    for (int ruleIndex = 0; ruleIndex < runCount; ruleIndex++) {
      currentRule = rules.get(ruleIndex);
      int matches = 0;
      long sentences = 0;
      final long startTime = System.currentTimeMillis();
      try {
        isr = getInputStreamReader(filename, encoding, isr);
        br = new BufferedReader(isr);
        String line;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
          sb.append(line);
          lineCount++;    
          // to detect language from the first input line
          if (lineCount == 1 && autoDetect) {     
              Language language = getLanguageFromString(line, false);
              if (language == null) {
                  System.err.println("Could not detect language well enough, using English");
                  language = Language.ENGLISH;
              }
              System.out.println("Language used is: " + language.getName());
              language.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(
                        singleLineBreakMarksParagraph);
              try {
                  changeLanguage(language, motherTongue, disabledRules, enabledRules);
              } catch (SAXException e) {
                  e.printStackTrace();
              } catch (ParserConfigurationException e) {
                  e.printStackTrace();
              }
          }
          sb.append('\n');
          tmpLineOffset++;
          if (lt.getLanguage().getSentenceTokenizer().singleLineBreaksMarksPara()) {
            matches = handleLine(matches, lineOffset, sb);
            sentences += lt.getSentenceCount();
            if (profileRules) {
              sentences += lt.sentenceTokenize(sb.toString()).size();
            }
            if (listUnknownWords && !taggerOnly) {
              for (String word : lt.getUnknownWords())
                if (!unknownWords.contains(word)) {
                  unknownWords.add(word);
                }
            }
            sb = new StringBuilder();
            lineOffset = tmpLineOffset;
          } else {
            if ("".equals(line) || sb.length() >= MAX_FILE_SIZE) {
              matches = handleLine(matches, lineOffset, sb);
              sentences += lt.getSentenceCount();
              if (profileRules) {
                sentences += lt.sentenceTokenize(sb.toString()).size();
              }
              if (listUnknownWords && !taggerOnly) {
                for (String word : lt.getUnknownWords())
                  if (!unknownWords.contains(word)) {
                    unknownWords.add(word);
                  }
              }
              sb = new StringBuilder();
              lineOffset = tmpLineOffset;
            }
          }
        }
      } finally {

        if (sb.length() > 0) {
          matches = handleLine(matches, tmpLineOffset - 1, sb);
          sentences += lt.getSentenceCount();
          if (profileRules) {
            sentences += lt.sentenceTokenize(sb.toString()).size();
          }
          if (listUnknownWords && !taggerOnly) {
            for (String word : lt.getUnknownWords())
              if (!unknownWords.contains(word)) {
                unknownWords.add(word);
              }
          }
        }

        printTimingInformation(listUnknownWords, rules, unknownWords, ruleIndex, matches, sentences, startTime);

        if (br != null) {
          br.close();
        }
        if (isr != null) {
          isr.close();
        }
      }
    }
  }

  private InputStreamReader getInputStreamReader(String filename, String encoding, InputStreamReader isr) throws UnsupportedEncodingException, FileNotFoundException {
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
        isr = new InputStreamReader(new BufferedInputStream(System.in),
            encoding);
      } else {
        isr = new InputStreamReader(new BufferedInputStream(System.in));
      }
    }
    return isr;
  }

  private void printTimingInformation(final boolean listUnknownWords, final List<Rule> rules,
      final List<String> unknownWords, final int ruleIndex, final int matches, final long sentences, final long startTime) {
    if (!applySuggestions) {
      final long endTime = System.currentTimeMillis();
      final long time = endTime - startTime;
      final float timeInSeconds = time / 1000.0f;
      final float sentencesPerSecond = sentences / timeInSeconds;
      if (apiFormat) {
        System.out.println("<!--");
      }
      if (profileRules) {
        //TODO: run 10 times, line in runOnce mode, and use median
        System.out.printf(Locale.ENGLISH,
            "%s\t%d\t%d\t%d\t%.1f", rules.get(ruleIndex).getId(),
            time, sentences, matches, sentencesPerSecond);
        System.out.println();
      } else {
        System.out.printf(Locale.ENGLISH,
            "Time: %dms for %d sentences (%.1f sentences/sec)", time,
            sentences, sentencesPerSecond);
        System.out.println();
      }
      if (listUnknownWords) {
        Collections.sort(unknownWords);
        System.out.println("Unknown words: " + unknownWords);
      }
      if (apiFormat) {
        System.out.println("-->");
      }
    }
  }

  private int handleLine(final int matchNo, final int lineOffset,
      final StringBuilder sb) throws IOException {
    int matches = matchNo;
    if (applySuggestions) {
      System.out.print(Tools.correctText(StringTools.filterXML(sb.toString()),
          lt));
    } else if (profileRules) {
      matches += Tools.profileRulesOnLine(StringTools.filterXML(sb.toString()), 
          lt, currentRule);
    } else if (!taggerOnly) {
      if (matches == 0) {
        matches += Tools.checkText(StringTools.filterXML(sb.toString()), lt,
            apiFormat, -1, lineOffset, matches,
            StringTools.XmlPrintMode.START_XML);
      } else {
        matches += Tools.checkText(StringTools.filterXML(sb.toString()), lt,
            apiFormat, -1, lineOffset, matches,
            StringTools.XmlPrintMode.CONTINUE_XML);
      }
    } else {
      Tools.tagText(StringTools.filterXML(sb.toString()), lt);
    }
    return matches;
  }

  private void runRecursive(final String filename, final String encoding,
      final boolean listUnknown) throws IOException,
      ParserConfigurationException, SAXException {
    final File dir = new File(filename);
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir.getAbsolutePath()
          + " is not a directory, cannot use recursion");
    }
    final File[] files = dir.listFiles();
    for (final File file : files) {
      if (file.isDirectory()) {
        runRecursive(file.getAbsolutePath(), encoding, listUnknown);
      } else {
        runOnFile(file.getAbsolutePath(), encoding, listUnknown);
      }
    }
  }

  /**
   * Loads filename and filters out XML. Note that the XML
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
    if (!apiFormat && !applySuggestions) {
      System.out.println("Working on " + filename + "...");
    }
    final String fileContents = StringTools.readFile(new FileInputStream(
        filename), encoding);
    return StringTools.filterXML(fileContents);
  }

  private static void exitWithUsageMessage() {
    System.out
    .println("Usage: java de.danielnaber.languagetool.Main "
        + "[-r|--recursive] [-v|--verbose] [-l|--language LANG] [-m|--mothertongue LANG] [-d|--disable RULES] [-adl|--autoDetect] "
        + "[-e|--enable RULES] [-c|--encoding] [-u|--list-unknown] [-t|--taggeronly] [-b] [--api] [-a|--apply] "             
        +    "[-b2|--bitext] <file>");
    System.exit(1);
  }

  /**
   * Command line tool to check plain text files.
   */
  public static void main(final String[] args) throws IOException,
  ParserConfigurationException, SAXException {
    if (args.length < 1 || args.length > 10) {
      exitWithUsageMessage();
    }
    boolean verbose = false;
    boolean recursive = false;
    boolean taggerOnly = false;
    boolean singleLineBreakMarksParagraph = false;
    boolean apiFormat = false;
    boolean listUnknown = false;
    boolean applySuggestions = false;
    boolean profile = false;
    boolean bitext = false;
    boolean autoDetect = false;
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
      } else if (args[i].equals("-adl") || args[i].equals("--autoDetect")) {    // set autoDetect flag
        // also initialize the other language profiles for the LanguageIdentifier
        LanguageIdentifierTools.addLtProfiles();
        autoDetect = true;
      } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
        verbose = true;
      } else if (args[i].equals("-t") || args[i].equals("--taggeronly")) {
        taggerOnly = true;
        if (listUnknown) {
          throw new IllegalArgumentException(
          "You cannot list unknown words when tagging only.");
        }
        if (applySuggestions) {
          throw new IllegalArgumentException(
          "You cannot apply suggestions when tagging only.");
        }
      } else if (args[i].equals("-r") || args[i].equals("--recursive")) {
        recursive = true;
      } else if (args[i].equals("-b2") || args[i].equals("--bitext")) {
        bitext = true;        
      } else if (args[i].equals("-d") || args[i].equals("--disable")) {
        if (enabledRules.length > 0) {
          throw new IllegalArgumentException(
          "You cannot specify both enabled and disabled rules");
        }
        checkArguments("-d/--disable", i, args);
        final String rules = args[++i];
        disabledRules = rules.split(",");
      } else if (args[i].equals("-e") || args[i].equals("--enable")) {
        if (disabledRules.length > 0) {
          throw new IllegalArgumentException(
          "You cannot specify both enabled and disabled rules");
        }
        checkArguments("-e/--enable", i, args);
        final String rules = args[++i];
        enabledRules = rules.split(",");
      } else if (args[i].equals("-l") || args[i].equals("--language")) {
        checkArguments("-l/--language", i, args);
        language = getLanguageOrExit(args[++i]);
      } else if (args[i].equals("-m") || args[i].equals("--mothertongue")) {
        checkArguments("-m/--mothertongue", i, args);
        motherTongue = getLanguageOrExit(args[++i]);
      } else if (args[i].equals("-c") || args[i].equals("--encoding")) {
        checkArguments("-c/--encoding", i, args);
        encoding = args[++i];
      } else if (args[i].equals("-u") || args[i].equals("--list-unknown")) {
        listUnknown = true;
        if (taggerOnly) {
          throw new IllegalArgumentException(
          "You cannot list unknown words when tagging only.");
        }
      } else if (args[i].equals("-b")) {
        singleLineBreakMarksParagraph = true;
      } else if (args[i].equals("--api")) {
        apiFormat = true;
        if (applySuggestions) {
          throw new IllegalArgumentException(
          "API format makes no sense for automatic application of suggestions.");
        }
      } else if (args[i].equals("-a") || args[i].equals("--apply")) {
        applySuggestions = true;
        if (taggerOnly) {
          throw new IllegalArgumentException(
          "You cannot apply suggestions when tagging only.");
        }
        if (apiFormat) {
          throw new IllegalArgumentException(
          "API format makes no sense for automatic application of suggestions.");
        }
      } else if (args[i].equals("-p") || args[i].equals("--profile")) {
        profile = true;        
        if (apiFormat) {
          throw new IllegalArgumentException(
          "API format makes no sense for profiling.");
        }
        if (applySuggestions) {
          throw new IllegalArgumentException(
          "Applying suggestions makes no sense for profiling.");
        }
        if (taggerOnly) {
          throw new IllegalArgumentException(
          "Tagging makes no sense for profiling.");
        }        
      }  else if (i == args.length - 1) {
        filename = args[i];
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
        if (!autoDetect) {
            System.err.println("No language specified, using English");
        }
      } 
      language = Language.ENGLISH;
    } else if (!apiFormat && !applySuggestions) {
      System.out.println("Expected text language: " + language.getName());
    }    
    
    language.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(
        singleLineBreakMarksParagraph);
    final Main prg = new Main(verbose, taggerOnly, language, motherTongue,
        disabledRules, enabledRules, apiFormat, applySuggestions, autoDetect, singleLineBreakMarksParagraph);
    prg.setListUnknownWords(listUnknown);
    if (profile) {
      prg.setProfilingMode();
    }
    if (bitext) {      
      if (motherTongue == null) {
        throw new IllegalArgumentException(
        "You have to set the source language (as mother tongue).");
      }
      prg.setBitextMode(motherTongue, disabledRules, enabledRules);
    }
    if (recursive) {
      prg.runRecursive(filename, encoding, listUnknown);
    } else {
      prg.runOnFile(filename, encoding, listUnknown);
    }
  }

  private static void checkArguments(String option, int argParsingPos, String[] args) {
    if (argParsingPos + 1 >= args.length) {
      throw new IllegalArgumentException("Missing argument to " + option + " command line option.");
    }
  }

  // for language auto detect
  // TODO: alter tika's language profiles so they are in line with LT's supported languages
  private static Language getLanguageFromFile(String filename, String encoding)    throws IOException {
      Language lang = null;
      LanguageIdentifier li;
      String text = StringTools.readFile(new FileInputStream(filename), encoding);
      li = new LanguageIdentifier(text);
      lang = Language.getLanguageForShortName(li.getLanguage());      
      return lang;
  }
  
  private static Language getLanguageFromString(String string, boolean print) {
      LanguageIdentifier li = new LanguageIdentifier(string);
      Language lang = Language.getLanguageForShortName(li.getLanguage());
      return lang;
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
  
  private void changeLanguage(Language language, Language motherTongue, 
          String[] disabledRules, String[] enabledRules ) throws IOException, SAXException, ParserConfigurationException {
      lt = new JLanguageTool(language, motherTongue);
      lt.activateDefaultPatternRules();
      lt.activateDefaultFalseFriendRules();
      selectRules(lt, disabledRules, enabledRules);
      if (verbose) {
        lt.setOutput(System.err);
      }
  }

}
