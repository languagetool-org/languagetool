/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.remote;

import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes correct sentences, introduces errors (e.g. confusion pairs), and
 * evaluates the LT rules.
 */
public class ArtificialErrorEval {

  static String[] words = new String[2];
  static String[] fakeRuleIDs = new String[2];
  static List<String> classifyTypes = Arrays.asList("TP", "FP", "TN", "FN", "TPs");
  static int[][] results = new int[2][5]; // word0/word1 ; TP/FP/TN/FN/TP with expected suggestion
  static RemoteLanguageTool lt;
  static int maxLines = 1000000; // decrease this number for testing
  static boolean verboseOutput = false;
  static boolean undirectional = false;
  static Pattern pWordboundaries = Pattern.compile("\\b.+\\b");
  static int countLine = 0;
  static List<String> onlyRules = new ArrayList<String>();

  public static void main(String[] args) throws IOException {
    if (args.length < 4 || args.length > 8) {
      System.out.println("Usage: " + ArtificialErrorEval.class.getSimpleName()
          + " <language code> <file> <string1> <string2> <options>");
      System.out.println("  <language code>, e.g. en, en-US, de, fr...");
      System.out.println("  <file> is a file with correct sentences, once sentence per line; errors will be "
          + "introduced and each line will be checked");
      System.out.println("  <string1> is the string to be replaced by <string2>, word boundaries will be "
          + "assumed at the start and the end of the strings");
      System.out.println("  <options>");
      System.out.println("    -v      verbose, print all false positive or false negative sentences");
      System.out.println("    -u      unidirectional, analyze only rules for string1 (wrong) -> string2 (correct)");
      System.out.println("    -r      list of comma-separated rules to be considered");
      System.exit(1);
    }
    for (int k = 4; k < args.length; k++) {
      if (args[k].contentEquals("-v")) {
        verboseOutput = true;
      }
      if (args[k].contentEquals("-u")) {
        undirectional = true;
      }
      if (args[k].contentEquals("-r")) {
        onlyRules = Arrays.asList(args[k + 1].split(","));
      }
    }
    long start = System.currentTimeMillis();
    words[0] = args[2];
    words[1] = args[3];
    fakeRuleIDs[0] = "rules_" + words[0] + "->" + words[1]; // rules in one direction
    fakeRuleIDs[1] = "rules_" + words[1] + "->" + words[0]; // rules in the other direction
    lt = new RemoteLanguageTool(Tools.getUrl("http://localhost:8081"));
    CheckConfiguration config = new CheckConfigurationBuilder(args[0]).disabledRuleIds("WHITESPACE_RULE").build();
    List<String> lines = Files.readAllLines(Paths.get(args[1]));
    final Pattern p0;
    Matcher mWordBoundaries = pWordboundaries.matcher(words[0]);
    if (mWordBoundaries.matches()) {
      p0 = Pattern.compile("\\b" + words[0] + "\\b", Pattern.CASE_INSENSITIVE);
    } else {
      p0 = Pattern.compile(words[0], Pattern.CASE_INSENSITIVE);
    }
    final Pattern p1;
    mWordBoundaries = pWordboundaries.matcher(words[1]);
    if (mWordBoundaries.matches()) {
      p1 = Pattern.compile("\\b" + words[1] + "\\b", Pattern.CASE_INSENSITIVE);
    } else {
      p1 = Pattern.compile(words[1], Pattern.CASE_INSENSITIVE);
    }
    for (String line : lines) {
      countLine++;
      if (countLine > maxLines) {
        break;
      }
      boolean foundSomething = false;
      if (words[0].length() > 0) {
        Matcher m = p0.matcher(line);
        while (m.find()) {
          foundSomething = true;
          analyzeSentence(line, 0, m.start(), config);
        }
      }
      if (words[1].length() > 0) {
        Matcher m = p1.matcher(line);
        while (m.find()) {
          foundSomething = true;
          analyzeSentence(line, 1, m.start(), config);
        }
      }
      if (!foundSomething) {
        // printSentenceOutput("Ignored, no error", line, "");
      }
    }
    // print results
    int oneOrTwo = (undirectional ? 1 : 2);
    for (int i = 0; i < oneOrTwo; i++) {
      System.out.println("-------------------------------------");
      System.out.println("Results for " + fakeRuleIDs[i]);
      System.out.println("TP (with expected suggestion): " + results[i][4]);
      for (int j = 0; j < 4; j++) {
        System.out.println(classifyTypes.get(j) + ": " + results[i][j]);
      }
      float precision = results[i][classifyTypes.indexOf("TP")]
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FP")]);
      float recall = results[i][classifyTypes.indexOf("TP")]
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FN")]);
      float expectedSuggestionPercentage = (float) results[i][classifyTypes.indexOf("TPs")]
          / results[i][classifyTypes.indexOf("TP")];
      System.out.println("Precision: " + String.format("%.4f", precision));
      System.out.println("Recall: " + String.format("%.4f", recall));
      System.out.println("TP with expected suggestion: " + String.format("%.4f", expectedSuggestionPercentage));
    }
    float time = (float) ((System.currentTimeMillis() - start) / 1000.0);
    System.out.println("-------------------------------------");
    System.out.println("Total time: " + String.format("%.2f", time) + " seconds");
  }

  private static void analyzeSentence(String correctSentence, int j, int fromPos, CheckConfiguration config)
      throws IOException {
    // Correct sentence
    if (!undirectional || j == 0) {
      List<RemoteRuleMatch> matchesCorrect = lt.check(correctSentence, config).getMatches();
      List<String> ruleIDs = ruleIDsAtPos(matchesCorrect, fromPos, words[1 - j]);
      if (ruleIDs.size() > 0) {
        results[j][classifyTypes.indexOf("FP")]++;
        printSentenceOutput("FP", correctSentence, fakeRuleIDs[j] + ":" + String.join(",", ruleIDs));
      } else {
        results[j][classifyTypes.indexOf("TN")]++;
        // Too verbose...
        // printSentenceOutput("TN", correctSentence, fakeRuleIDs[j]);
      }
    }

    // Wrong sentence
    if (!undirectional || j == 1) {
      String replaceWith = words[1 - j];
      String originalString = correctSentence.substring(fromPos, fromPos + words[j].length());
      if (StringTools.isCapitalizedWord(originalString)) {
        replaceWith = StringTools.uppercaseFirstChar(replaceWith);
      }
      if (StringTools.isAllUppercase(originalString)) {
        replaceWith = replaceWith.toUpperCase();
      }
      String wrongSentence = correctSentence.substring(0, fromPos) + replaceWith
          + correctSentence.substring(fromPos + words[j].length(), correctSentence.length());
      if (wrongSentence.equals(correctSentence)) {
        // Should not happen
        printSentenceOutput("Error: word cannot be replaced", correctSentence, "");
        return;
      }
      List<RemoteRuleMatch> matchesWrong = lt.check(wrongSentence, config).getMatches();
      List<String> ruleIDs = ruleIDsAtPos(matchesWrong, fromPos, words[j]);
      if (ruleIDs.size() > 0) {
        results[1 - j][classifyTypes.indexOf("TP")]++;
        if (isExpectedSuggestionAtPos(matchesWrong, fromPos, words[j], wrongSentence, correctSentence)) {
          results[1 - j][classifyTypes.indexOf("TPs")]++;
          printSentenceOutput("TP", wrongSentence, fakeRuleIDs[1 - j] + ":" + String.join(",", ruleIDs));
        } else {
          printSentenceOutput("TP no expected suggestion", wrongSentence,
              fakeRuleIDs[1 - j] + ":" + String.join(",", ruleIDs));
        }
      } else {
        results[1 - j][classifyTypes.indexOf("FN")]++;
        printSentenceOutput("FN", wrongSentence, fakeRuleIDs[1 - j]);
      }
    }
  }

  private static void printSentenceOutput(String classification, String sentence, String ruleIds) {
    if (verboseOutput) {
      System.out.println(countLine + ". " + classification + ": " + sentence + " –– " + ruleIds);
    }
  }

  private static List<String> ruleIDsAtPos(List<RemoteRuleMatch> matchesCorrect, int pos, String expectedSuggestion) {
    List<String> ruleIDs = new ArrayList<>();
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
        if (!onlyRules.isEmpty() && !onlyRules.contains(match.getRuleId())) {
          continue;
        }
        String subId = null;
        List<String> replacements = null;
        try {
          subId = match.getRuleSubId().get();
        } catch (NoSuchElementException e) {
          // System.out.println("Exception, skipping '" + countLine + "': ");
          // e.printStackTrace();
        }
        try {
          replacements = match.getReplacements().get();
        } catch (NoSuchElementException e) {
        }
        boolean containsDesiredSuggestion = false;
        if (replacements != null) {
          for (String replacement : replacements) {
            // FIXME; if (replacement.contains(expectedSuggestion.strip())) {
            if (replacement.contains(expectedSuggestion.trim())) {
              containsDesiredSuggestion = true;
            }
          } 
        }
        if (!containsDesiredSuggestion) {
          continue;
        }
        if (subId != null) {
          ruleIDs.add(match.getRuleId() + "[" + match.getRuleSubId().get() + "]");
        } else {
          ruleIDs.add(match.getRuleId());
        }
      }
    }
    return ruleIDs;
  }

  private static boolean isExpectedSuggestionAtPos(List<RemoteRuleMatch> matchesCorrect, int pos,
      String expectedSuggestion, String wrongSentence, String correctSentence) {
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
        for (String s : match.getReplacements().get()) {
          // check that the replacement rebuilds the original correct sentence
          String correctedSentence = wrongSentence.substring(0, match.getErrorOffset()) + s
              + wrongSentence.substring(match.getErrorOffset() + match.getErrorLength(), wrongSentence.length());
          if (correctedSentence.equals(correctSentence)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
