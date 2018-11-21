/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.DetectedLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.bitext.BitextReader;
import org.languagetool.bitext.StringPair;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tools.ContextTools;
import org.languagetool.tools.RuleMatchAsXmlSerializer;
import org.languagetool.tools.RuleMatchesAsJsonSerializer;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @since 2.3
 */
public final class CommandLineTools {

  private static final int DEFAULT_CONTEXT_SIZE = 45;

  private CommandLineTools() {
  }

  /**
   * Tags text using the LanguageTool tagger, printing results to System.out.
   *
   * @param contents Text to tag.
   * @param lt LanguageTool instance
   */
  public static void tagText(String contents, JLanguageTool lt) throws IOException {
    AnalyzedSentence analyzedText;
    List<String> sentences = lt.sentenceTokenize(contents);
    for (String sentence : sentences) {
      analyzedText = lt.getAnalyzedSentence(sentence);
      System.out.println(analyzedText);
    }
  }

  public static int checkText(String contents, JLanguageTool lt) throws IOException {
    return checkText(contents, lt, false, false, -1, 0, 0, StringTools.ApiPrintMode.NORMAL_API, false, Collections.<String>emptyList());
  }

  public static int checkText(String contents, JLanguageTool lt,
                              boolean isXmlFormat, boolean isJsonFormat, int lineOffset) throws IOException {
    return checkText(contents, lt, isXmlFormat, isJsonFormat, -1, lineOffset, 0, StringTools.ApiPrintMode.NORMAL_API, false, Collections.<String>emptyList());
  }
  
  public static int checkText(String contents, JLanguageTool lt,
          boolean isXmlFormat, boolean isJsonFormat, int lineOffset, boolean listUnknownWords) throws IOException {
    return checkText(contents, lt, isXmlFormat, isJsonFormat, -1, lineOffset, 0, StringTools.ApiPrintMode.NORMAL_API, listUnknownWords, Collections.<String>emptyList());
}

  /**
   * Check the given text and print results to System.out.
   *
   * @param contents a text to check (may be more than one sentence)
   * @param lt Initialized LanguageTool
   * @param isXmlFormat whether to print the result in XML format
   * @param isJsonFormat whether to print the result in JSON format
   * @param contextSize error text context size: -1 for default
   * @param lineOffset line number offset to be added to line numbers in matches
   * @param prevMatches number of previously matched rules
   * @param apiMode mode of xml/json printout for simple xml/json output
   * @return Number of rule matches to the input text.
   */
  public static int checkText(String contents, JLanguageTool lt,
                              boolean isXmlFormat, boolean isJsonFormat, int contextSize, int lineOffset,
                              int prevMatches, StringTools.ApiPrintMode apiMode,
                              boolean listUnknownWords, List<String> unknownWords) throws IOException {
    if (contextSize == -1) {
      contextSize = DEFAULT_CONTEXT_SIZE;
    }
    long startTime = System.currentTimeMillis();
    List<RuleMatch> ruleMatches = lt.check(contents);
    // adjust line numbers
    for (RuleMatch r : ruleMatches) {
      r.setLine(r.getLine() + lineOffset);
      r.setEndLine(r.getEndLine() + lineOffset);
    }
    if (isXmlFormat) {
      if (listUnknownWords && apiMode == StringTools.ApiPrintMode.NORMAL_API) {
        unknownWords = lt.getUnknownWords();
      }
      RuleMatchAsXmlSerializer serializer = new RuleMatchAsXmlSerializer();
      String xml = serializer.ruleMatchesToXml(ruleMatches, contents,
              contextSize, apiMode, lt.getLanguage(), unknownWords);
      PrintStream out = new PrintStream(System.out, true, "UTF-8");
      out.print(xml);
    } else if (isJsonFormat) {
      RuleMatchesAsJsonSerializer serializer = new RuleMatchesAsJsonSerializer();
      String json = serializer.ruleMatchesToJson(ruleMatches, contents, contextSize,
        new DetectedLanguage(lt.getLanguage(), lt.getLanguage()));
      PrintStream out = new PrintStream(System.out, true, "UTF-8");
      out.print(json);
    } else {
      printMatches(ruleMatches, prevMatches, contents, contextSize);
    }

    //display stats if it's not in a buffered mode
    if (apiMode == StringTools.ApiPrintMode.NORMAL_API && !isJsonFormat) {
      SentenceTokenizer sentenceTokenizer = lt.getLanguage().getSentenceTokenizer();
      int sentenceCount = sentenceTokenizer.tokenize(contents).size();
      displayTimeStats(startTime, sentenceCount, isXmlFormat);
    }
    return ruleMatches.size();
  }

  private static void displayTimeStats(long startTime,
                                       long sentCount, boolean isXmlFormat) {
    long endTime = System.currentTimeMillis();
    long time = endTime - startTime;
    float timeInSeconds = time / 1000.0f;
    float sentencesPerSecond = sentCount / timeInSeconds;
    if (isXmlFormat) {
      System.out.println("<!--");
    }
    System.out.printf(Locale.ENGLISH,
            "Time: %dms for %d sentences (%.1f sentences/sec)", time,
            sentCount, sentencesPerSecond);
    System.out.println();
    if (isXmlFormat) {
      System.out.println("-->");
    }
  }

  /**
   * Displays matches in a simple text format.
   * @param ruleMatches Matches from rules.
   * @param prevMatches Number of previously found matches.
   * @param contents  The text that was checked.
   * @param contextSize The size of contents displayed.
   * @since 1.0.1
   */
  private static void printMatches(List<RuleMatch> ruleMatches,
                                   int prevMatches, String contents, int contextSize) {
    int i = 1;
    ContextTools contextTools = new ContextTools();
    contextTools.setContextSize(contextSize);
    for (RuleMatch match : ruleMatches) {
      Rule rule = match.getRule();
      String output = i + prevMatches + ".) Line " + (match.getLine() + 1) + ", column "
              + match.getColumn() + ", Rule ID: " + rule.getId();
      if (rule instanceof AbstractPatternRule) {
        AbstractPatternRule pRule = (AbstractPatternRule) rule;
        if (pRule.getSubId() != null) {
          output += "[" + pRule.getSubId() + "]";
        }
      }
      System.out.println(output);
      String msg = match.getMessage();
      msg = msg.replaceAll("</?suggestion>", "'");
      System.out.println("Message: " + msg);
      List<String> replacements = match.getSuggestedReplacements();
      if (!replacements.isEmpty()) {
        System.out.println("Suggestion: " + String.join("; ", replacements));
      }
      System.out.println(contextTools.getPlainTextContext(match.getFromPos(), match.getToPos(), contents));
      if (match.getUrl() != null) {
        System.out.println("More info: " + match.getUrl());
      } else if (rule.getUrl() != null) {
        System.out.println("More info: " + rule.getUrl());
      }
      if (i < ruleMatches.size()) {
        System.out.println();
      }
      i++;
    }
  }

  /**
   * Checks the bilingual input (bitext) and displays the output (considering the target 
   * language) in API format or in the simple text format.
   *
   * NOTE: the positions returned by the rule matches are adjusted
   * according to the data returned by the reader.
   *
   * @param reader   Reader of bitext strings.
   * @param srcLt Source JLanguageTool (used to analyze the text).
   * @param trgLt Target JLanguageTool (used to analyze the text).
   * @param bRules  Bilingual rules used in addition to target standard rules.
   * @return The number of rules matched on the bitext.
   * @since 1.0.1
   */
  public static int checkBitext(BitextReader reader,
                                JLanguageTool srcLt, JLanguageTool trgLt,
                                List<BitextRule> bRules,
                                boolean isXmlFormat) throws IOException {
    long startTime = System.currentTimeMillis();
    int contextSize = DEFAULT_CONTEXT_SIZE;
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int matchCount = 0;
    int sentCount = 0;
    RuleMatchAsXmlSerializer serializer = new RuleMatchAsXmlSerializer();
    PrintStream out = new PrintStream(System.out, true, "UTF-8");
    if (isXmlFormat) {
      out.print(serializer.getXmlStart(null, null));
    }
    for (StringPair srcAndTrg : reader) {
      List<RuleMatch> curMatches = Tools.checkBitext(
              srcAndTrg.getSource(), srcAndTrg.getTarget(),
              srcLt, trgLt, bRules);
      List<RuleMatch> fixedMatches = new ArrayList<>();
      for (RuleMatch thisMatch : curMatches) {
        fixedMatches.add(
                trgLt.adjustRuleMatchPos(thisMatch,
                        reader.getSentencePosition(),
                        reader.getColumnCount(),
                        reader.getLineCount(),
                        reader.getCurrentLine(), null));
      }
      ruleMatches.addAll(fixedMatches);
      if (fixedMatches.size() > 0) {
        if (isXmlFormat) {
          String xml = serializer.ruleMatchesToXmlSnippet(fixedMatches,
                  reader.getCurrentLine(), contextSize);
          out.print(xml);
        } else {
          printMatches(fixedMatches, matchCount, reader.getCurrentLine(), contextSize);
          matchCount += fixedMatches.size();
        }
      }
      sentCount++;
    }
    displayTimeStats(startTime, sentCount, isXmlFormat);
    if (isXmlFormat) {
      out.print(serializer.getXmlEnd());
    }
    return ruleMatches.size();
  }

  /**
   * Simple rule profiler - used to run LT on a corpus to see which
   * rule takes most time. Prints results to System.out.
   *
   * @param contents text to check
   * @param lt instance of LanguageTool
   */
  public static void profileRulesOnText(String contents,
                                        JLanguageTool lt) throws IOException {
    long[] workTime = new long[10];
    List<Rule> rules = lt.getAllActiveRules();
    int ruleCount = rules.size();
    System.out.printf("Testing %d rules%n", ruleCount);
    System.out.println("Rule ID\tTime\tSentences\tMatches\tSentences per sec.");
    List<String> sentences = lt.sentenceTokenize(contents);
    for (Rule rule : rules) {
      if (rule instanceof TextLevelRule) {
        continue; // profile rules for sentences only
      }
      int matchCount = 0;
      for (int k = 0; k < 10; k++) {
        long startTime = System.currentTimeMillis();
        for (String sentence : sentences) {
          matchCount += rule.match
                  (lt.getAnalyzedSentence(sentence)).length;
        }
        long endTime = System.currentTimeMillis();
        workTime[k] = endTime - startTime;
      }
      long time = median(workTime);
      float timeInSeconds = time / 1000.0f;
      float sentencesPerSecond = sentences.size() / timeInSeconds;
      System.out.printf(Locale.ENGLISH,
              "%s\t%d\t%d\t%d\t%.1f", rule.getId(),
              time, sentences.size(), matchCount, sentencesPerSecond);
      System.out.println();
    }
  }

  private static long median(long[] m) {
    Arrays.sort(m);
    int middle = m.length / 2;  // subscript of middle element
    if (m.length % 2 == 1) {
      // Odd number of elements -- return the middle one.
      return m[middle];
    }
    return (m[middle-1] + m[middle]) / 2;
  }

  /**
   * Automatically applies suggestions to the bilingual text.
   * Note: if there is more than one suggestion, always the first
   * one is applied, and others ignored silently.
   * Prints results to System.out.
   *
   * @param reader a bitext file reader
   * @param sourceLt Initialized source JLanguageTool object
   * @param targetLt Initialized target JLanguageTool object
   * @param bRules  List of all BitextRules to use
   */
  public static void correctBitext(BitextReader reader,
                                   JLanguageTool sourceLt, JLanguageTool targetLt,
                                   List<BitextRule> bRules) throws IOException {
    for (StringPair srcAndTrg : reader) {
      List<RuleMatch> curMatches = Tools.checkBitext(
              srcAndTrg.getSource(), srcAndTrg.getTarget(),
              sourceLt, targetLt, bRules);
      List<RuleMatch> fixedMatches = new ArrayList<>();
      for (RuleMatch thisMatch : curMatches) {
        fixedMatches.add(
                targetLt.adjustRuleMatchPos(thisMatch,
                        0, //don't need to adjust at all, we have zero offset related to trg sentence 
                        reader.getTargetColumnCount(),
                        reader.getLineCount(),
                        reader.getCurrentLine(), null));
      }
      if (fixedMatches.size() > 0) {
        System.out.println(correctTextFromMatches(srcAndTrg.getTarget(), fixedMatches));
      } else {
        System.out.println(srcAndTrg.getTarget());
      }
    }
  }

  private static String correctTextFromMatches(
          String contents, List<RuleMatch> matches) {
    StringBuilder sb = new StringBuilder(contents);
    List<String> errors = new ArrayList<>();
    for (RuleMatch rm : matches) {
      List<String> replacements = rm.getSuggestedReplacements();
      if (!replacements.isEmpty()) {
        errors.add(sb.substring(rm.getFromPos(), rm.getToPos()));
      }
    }
    int offset = 0;
    int counter = 0;
    for (RuleMatch rm : matches) {
      List<String> replacements = rm.getSuggestedReplacements();
      if (!replacements.isEmpty()) {
        //make sure the error hasn't been already corrected:
        if (errors.get(counter).equals(sb.substring(rm.getFromPos() - offset, rm.getToPos() - offset))) {
          sb.replace(rm.getFromPos() - offset, rm.getToPos() - offset, replacements.get(0));
          offset += (rm.getToPos() - rm.getFromPos()) - replacements.get(0).length();
        }
        counter++;
      }
    }
    return sb.toString();
  }

}
