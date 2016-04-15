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
package org.languagetool.commandline;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.bitext.TabBitextReader;
import org.languagetool.language.English;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.rules.Rule;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.tools.JnaTools;
import org.languagetool.tools.Tools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static org.languagetool.tools.StringTools.*;

/**
 * The command line tool to check plain text files.
 */
class Main {

  private final CommandLineOptions options;
  
  private MultiThreadedJLanguageTool lt;
  private boolean profileRules;
  private boolean bitextMode;
  private MultiThreadedJLanguageTool srcLt;
  private List<BitextRule> bRules;
  private Rule currentRule;

  Main(CommandLineOptions options) throws IOException {
    this.options = options;
    profileRules = false;
    bitextMode = false;
    srcLt = null;
    bRules = null;
    lt = new MultiThreadedJLanguageTool(options.getLanguage(), options.getMotherTongue());
    if (options.getRuleFile() != null) {
      addExternalRules(options.getRuleFile());
    }
    if (options.getLanguageModel() != null) {
      lt.activateLanguageModelRules(options.getLanguageModel());
    }
    Tools.selectRules(lt, options.getDisabledCategories(), options.getEnabledCategories(),
            new HashSet<>(options.getDisabledRules()), new HashSet<>(options.getEnabledRules()), options.isUseEnabledOnly());
  }

  private void addExternalRules(String filename) throws IOException {
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    try (InputStream is = new FileInputStream(filename)) {
      List<AbstractPatternRule> externalRules = ruleLoader.getRules(is, filename);
      for (AbstractPatternRule externalRule : externalRules) {
        lt.addRule(externalRule);
      }
    }
  }

  boolean isSpellCheckingActive() {
    List<Rule> rules = lt.getAllActiveRules();
    for (Rule rule : rules) {
      if (rule.isDictionaryBasedSpellingRule()) {
        return true;
      }
    }
    return false;
  }
  
  JLanguageTool getJLanguageTool() {
    return lt;
  }
  
  private void setListUnknownWords(boolean listUnknownWords) {
    lt.setListUnknownWords(listUnknownWords);
  }
  
  private void cleanUp() {
    if (lt != null) {
      lt.shutdown();
    }
    if (srcLt != null) {
      srcLt.shutdown();
    }
    JLanguageTool.removeTemporaryFiles();
  }
  
  private void setProfilingMode() {
    profileRules = true;
  }

  private void setBitextMode(Language sourceLang,
      List<String> disabledRules, List<String> enabledRules, File bitextRuleFile) throws IOException, ParserConfigurationException, SAXException {
    bitextMode = true;
    Language target = lt.getLanguage();
    lt = new MultiThreadedJLanguageTool(target, null);
    srcLt = new MultiThreadedJLanguageTool(sourceLang);
    Tools.selectRules(lt, disabledRules, enabledRules, true);
    Tools.selectRules(srcLt, disabledRules, enabledRules, true);
    bRules = Tools.getBitextRules(sourceLang, lt.getLanguage(), bitextRuleFile);

    List<BitextRule> bRuleList = new ArrayList<>(bRules);
    for (BitextRule bitextRule : bRules) {
      for (String disabledRule : disabledRules) {
        if (bitextRule.getId().equals(disabledRule)) {
          bRuleList.remove(bitextRule);
        }
      }
    }
    bRules = bRuleList;
    if (enabledRules.size() > 0) {
      bRuleList = new ArrayList<>();
      for (String enabledRule : enabledRules) {
        for (BitextRule bitextRule : bRules) {
          if (bitextRule.getId().equals(enabledRule)) {
            bRuleList.add(bitextRule);
          }
        }
      }
      bRules = bRuleList;
    }
  }

  private void runOnFile(String filename, String encoding,
      boolean xmlFiltering) throws IOException {
    if (bitextMode) {
      TabBitextReader reader = new TabBitextReader(filename, encoding);
      if (options.isApplySuggestions()) {
        CommandLineTools.correctBitext(reader, srcLt, lt, bRules);
      } else {
        CommandLineTools.checkBitext(reader, srcLt, lt, bRules, options.isApiFormat());
      }
    } else {
      String text = getFilteredText(filename, encoding, xmlFiltering);
      if (isStdIn(filename)) {
        System.err.println("Working on STDIN...");
      } else {
        System.err.println("Working on " + filename + "...");
      }
      if (options.isAutoDetect()) {
        Language language = detectLanguageOfString(text);
        if (language == null) {
          System.err.println("Could not detect language well enough, using English");
          language = new English();
        }
        changeLanguage(language, options.getMotherTongue(), options.getDisabledRules(), options.getEnabledRules());
        System.err.println("Using " + language.getName() + " for file " + filename);
      }
      if (options.isApplySuggestions()) {
        System.out.print(Tools.correctText(text, lt));
      } else if (profileRules) {
        CommandLineTools.profileRulesOnText(text, lt);
      } else if (!options.isTaggerOnly()) {
        CommandLineTools.checkText(text, lt, options.isApiFormat(), 0, options.isListUnknown());
      } else {
        CommandLineTools.tagText(text, lt);
      }
      if (options.isListUnknown() && !options.isApiFormat()) {
        System.out.println("Unknown words: " + lt.getUnknownWords());
      }
    }
  }

  private void runOnFileLineByLine(String filename, String encoding) throws IOException {
    System.err.println("Warning: running in line by line mode. Cross-paragraph checks will not work.\n");
    if (options.isVerbose()) {
      lt.setOutput(System.err);
    }
    if (!options.isApiFormat() && !options.isApplySuggestions()) {
      if (isStdIn(filename)) {
        System.err.println("Working on STDIN...");
      } else {
        System.err.println("Working on " + filename + "...");
      }
    }
    if (profileRules && isStdIn(filename)) {
      throw new IllegalArgumentException("Profiling mode cannot be used with input from STDIN");
    }
    int runCount = 1;
    List<Rule> rules = lt.getAllActiveRules();
    if (profileRules) {
      System.out.printf("Testing %d rules\n", rules.size());
      System.out.println("Rule ID\tTime\tSentences\tMatches\tSentences per sec.");
      runCount = rules.size();
    }
    int lineOffset = 0;
    int tmpLineOffset = 0;
    handleLine(XmlPrintMode.START_XML, 0, new StringBuilder());
    StringBuilder sb = new StringBuilder();
    for (int ruleIndex = 0; !rules.isEmpty() && ruleIndex < runCount; ruleIndex++) {
      currentRule = rules.get(ruleIndex);
      int matches = 0;
      long sentences = 0;
      long startTime = System.currentTimeMillis();
      try (
          InputStreamReader isr = getInputStreamReader(filename, encoding);
          BufferedReader br = new BufferedReader(isr)
      ) {
        String line;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
          sb.append(line);
          lineCount++;
          // to detect language from the first input line
          if (lineCount == 1 && options.isAutoDetect()) {
            Language language = detectLanguageOfString(line);
            if (language == null) {
              System.err.println("Could not detect language well enough, using English");
              language = new English();
            }
            System.err.println("Language used is: " + language.getName());
            language.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(
                    options.isSingleLineBreakMarksParagraph());
            changeLanguage(language, options.getMotherTongue(), options.getDisabledRules(), options.getEnabledRules());
          }
          sb.append('\n');
          tmpLineOffset++;

          if (isBreakPoint(line)) {
            matches += handleLine(XmlPrintMode.CONTINUE_XML, lineOffset, sb);
            sentences += lt.getSentenceCount();
            if (profileRules) {
              sentences += lt.sentenceTokenize(sb.toString()).size();
            }
            sb = new StringBuilder();
            lineOffset = tmpLineOffset;
          }
        }
      } finally {
        if (sb.length() > 0) {
          sentences += lt.getSentenceCount();
          if (profileRules) {
            sentences += lt.sentenceTokenize(sb.toString()).size();
          }
        }
        matches += handleLine(XmlPrintMode.END_XML, tmpLineOffset - 1, sb);
        // printTimingInformation(rules, ruleIndex, matches, sentences, startTime);
      }
    }
  }

  private int handleLine(XmlPrintMode mode, int lineOffset, StringBuilder sb) throws IOException {
    int matches = 0;
    String s = filterXML(sb.toString());
    if (options.isApplySuggestions()) {
        System.out.print(Tools.correctText(s, lt));
      } else if (profileRules) {
        matches += Tools.profileRulesOnLine(s, lt, currentRule);
      } else if (!options.isTaggerOnly()) {
        matches += CommandLineTools.checkText(s, lt, options.isApiFormat(), -1, lineOffset,
                matches, mode, options.isListUnknown(), Collections.<String>emptyList());
      } else {
        CommandLineTools.tagText(s, lt);
      }
    return matches;
  }

  private boolean isBreakPoint(String line) {
    return lt.getLanguage().getSentenceTokenizer().singleLineBreaksMarksPara() || "".equals(line);
  }

  private InputStreamReader getInputStreamReader(String filename, String encoding)
      throws UnsupportedEncodingException, FileNotFoundException {
    InputStreamReader isr;
    if (!isStdIn(filename)) {
      File file = new File(filename);
      if (encoding != null) {
        isr = new InputStreamReader(new BufferedInputStream(
            new FileInputStream(file)), encoding);
      } else {
        isr = new InputStreamReader(new BufferedInputStream(
            new FileInputStream(file)));
      }
    } else {
      if (encoding != null) {
        isr = new InputStreamReader(new BufferedInputStream(System.in), encoding);
      } else {
        isr = new InputStreamReader(new BufferedInputStream(System.in));
      }
    }
    return isr;
  }

  private boolean isStdIn(String filename) {
    return "-".equals(filename);
  }

  private void runRecursive(String filename, String encoding, boolean xmlFiltering) {
    File dir = new File(filename);
    File[] files = dir.listFiles();
    if (files == null) {
      throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory, cannot use recursion");
    }
    for (File file : files) {
      try {
        if (file.isDirectory()) {
          runRecursive(file.getAbsolutePath(), encoding, xmlFiltering);
        } else {
          if (options.isLineByLine()) {
            runOnFileLineByLine(file.getAbsolutePath(), encoding);
          } else {
            runOnFile(file.getAbsolutePath(), encoding, xmlFiltering);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Could not check text in file " + file, e);
      }
    }    
  }

  /**
   * Loads filename and filters out XML. Note that the XML
   * filtering can lead to incorrect positions in the list of matching rules.
   */
  private String getFilteredText(String filename, String encoding, boolean xmlFiltering) throws IOException {
    if (options.isVerbose()) {
      lt.setOutput(System.err);
    }
    // don't use StringTools.readStream() as that might add newlines which aren't there:
    String fileContents;
    String encodingName = encoding != null ? encoding : Charset.defaultCharset().name();
    if (isStdIn(filename)) {
      fileContents = streamToString(new BufferedInputStream(System.in), encodingName);
    } else {
      fileContents = streamToString(new FileInputStream(filename), encodingName);
    }
    if (xmlFiltering) {
      return filterXML(fileContents);
    } else {
      return fileContents;
    }
  }

  private void changeLanguage(Language language, Language motherTongue,
                              List<String> disabledRules, List<String> enabledRules) {
    try {
      lt = new MultiThreadedJLanguageTool(language, motherTongue);
      Tools.selectRules(lt, disabledRules, enabledRules, true);
      if (options.isVerbose()) {
        lt.setOutput(System.err);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not create LanguageTool instance for language " + language, e);
    }
  }

  /**
   * Command line tool to check plain text files.
   */
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    JnaTools.setBugWorkaroundProperty();
    CommandLineParser commandLineParser = new CommandLineParser();
    CommandLineOptions options = null;
    try {
       options = commandLineParser.parseOptions(args);
    } catch (WrongParameterNumberException e) {
      commandLineParser.printUsage();
      System.exit(1);
    } catch (IllegalArgumentException e) {
      System.err.println(e.toString());
      System.exit(1);
    } catch (UnknownParameterException e) {
      if (e.getMessage() != null) {
        System.err.println(e.getMessage());
      } else {
        System.err.println(e.toString());
      }
      commandLineParser.printUsage(System.err);
      System.exit(1);
    }
    if (options.isPrintUsage()) {
      commandLineParser.printUsage();
      System.exit(1);
    }
    if (options.isPrintVersion()) {
      System.out.println("LanguageTool version " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ")");
      System.exit(0);
    }
    if (options.isPrintLanguages()) {
      printLanguages();
      System.exit(0);
    }

    if (options.getFilename() == null) {
      options.setFilename("-");
    }

    String languageHint = null;
    if (options.getLanguage() == null) {
      if (!options.isApiFormat() && !options.isAutoDetect()) {
        System.err.println("No language specified, using English (no spell checking active, " +
                "specify a language variant like 'en-GB' if available)");
      }
      options.setLanguage(new English());
    } else if (!options.isApiFormat() && !options.isApplySuggestions()) {
      languageHint = "Expected text language: " + options.getLanguage().getName();
    }

    options.getLanguage().getSentenceTokenizer().setSingleLineBreaksMarksParagraph(
            options.isSingleLineBreakMarksParagraph());

    Main prg = new Main(options);
    if (options.getFalseFriendFile() != null) {
      List<AbstractPatternRule> ffRules = prg.lt.loadFalseFriendRules(options.getFalseFriendFile());
      for (AbstractPatternRule ffRule : ffRules) {
        prg.lt.addRule(ffRule);
      }
    }
    if (prg.lt.getAllActiveRules().size() == 0) {
      throw new RuntimeException("WARNING: No rules are active. Please make sure your rule ids are correct: " +
              options.getEnabledRules());
    }
    if (languageHint != null) {
      String spellHint = prg.isSpellCheckingActive() ?
              "" : " (no spell checking active, specify a language variant like 'en-GB' if available)";
      System.err.println(languageHint + spellHint);
    }
    prg.setListUnknownWords(options.isListUnknown());
    if (options.isProfile()) {
      prg.setProfilingMode();
    }
    if (options.isBitext()) {
      if (options.getMotherTongue() == null) {
        throw new IllegalArgumentException("You have to set the source language (as mother tongue) in bitext mode");
      }
      File bitextRuleFile = options.getBitextRuleFile() != null ? new File(options.getBitextRuleFile()) : null;
      prg.setBitextMode(options.getMotherTongue(), options.getDisabledRules(), options.getEnabledRules(), bitextRuleFile);
    }
    if (options.isRecursive()) {
      prg.runRecursive(options.getFilename(), options.getEncoding(), options.isXmlFiltering());
    } else {
      if (options.isLineByLine()) {
        prg.runOnFileLineByLine(options.getFilename(), options.getEncoding());
      } else {
        prg.runOnFile(options.getFilename(), options.getEncoding(), options.isXmlFiltering());
      }
    }
    prg.cleanUp();
  }

  private static void printLanguages() {
    List<String> languages = new ArrayList<>();
    for (Language language : Languages.get()) {
      languages.add(language.getShortNameWithCountryAndVariant() + " " + language.getName());
    }
    Collections.sort(languages);
    for (String s : languages) {
      System.out.println(s);
    }
  }

  private static Language detectLanguageOfString(String text) {
    LanguageIdentifier identifier = new LanguageIdentifier();
    return identifier.detectLanguage(text);
  }

}
