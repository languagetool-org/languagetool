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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes correct sentences, introduces errors (e.g. confusion pairs), 
 * and evaluates the LT rules.
 */
public class ArtificialErrorEval {

  static String[] words = new String[2];
  static String[] ruleIds = new String[2];
  static List<String> classifyTypes = Arrays.asList("TP", "FP", "TN", "FN", "TPs");
  static int[][] results = new int[2][5]; // word0/word1 ; TP/FP/TN/FN/TP with expected suggestion
  static RemoteLanguageTool lt;
  static int maxLines = 1000000; // decrease this number for testing
  static boolean verboseOutput = false;
  static boolean undirectional = false;
  static Pattern pWordboundaries = Pattern.compile("\\b.+\\b");

  public static void main(String[] args) throws IOException {
    if (args.length < 4 || args.length > 6) {
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
      System.exit(1);
    }
    for (int k = 4; k < args.length; k++) {
      if (args[k].contentEquals("-v")) {
        verboseOutput = true;
      }
      if (args[k].contentEquals("-u")) {
        undirectional = true;
      }
    }
    long start = System.currentTimeMillis();
    words[0] = args[2];
    words[1] = args[3];
    ruleIds[0] = "rules " + words[0] + " -> " + words[1]; // rules in one direction
    ruleIds[1] = "rules " + words[1] + " -> " + words[0]; // rules in the other direction
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
    int count = 0;
    for (String line : lines) {
      count++;
      if (verboseOutput && count % 500 == 0) {
        System.out.println("Read "+count+" lines from corpus");
      }
      if (count > maxLines) {
        break;
      }
      if (words[0].length() > 0) {
        Matcher m = p0.matcher(line);
        while (m.find()) {
          analyzeSentence(line, 0, m.start(), config);
        }
      }
      if (words[1].length() > 0) {
        Matcher m = p1.matcher(line);
        while (m.find()) {
          analyzeSentence(line, 1, m.start(), config);
        }
      }
    }
    // print results
    int oneOrTwo = (undirectional ? 1 : 2);
    for (int i = 0; i < oneOrTwo; i++) {
      System.out.println("-------------------------------------");
      System.out.println("Results for " + ruleIds[i]);
      System.out.println("TP (with expected suggestion): " + results[i][4]);
      for (int j = 0; j < 4; j++) {
        System.out.println(classifyTypes.get(j) + ": " + results[i][j]);
      }
      float precision = results[i][classifyTypes.indexOf("TP")]
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FP")]);
      float recall = results[i][classifyTypes.indexOf("TP")]
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FN")]);
      System.out.println("Precision: " + String.format("%.4f", precision));
      System.out.println("Recall: " + String.format("%.4f", recall));
    }
    float time = (float) ((System.currentTimeMillis() - start) / 1000.0);
    System.out.println("-------------------------------------");
    System.out.println("Total time: " + String.format("%.2f", time) + " seconds");
  }

  private static void analyzeSentence(String correctSentence, int j, int fromPos, CheckConfiguration config)
      throws IOException {
    // boolean isFP = false;
    // boolean isFN = false;
    // Correct sentence
    if (!undirectional || j == 0) {
      List<RemoteRuleMatch> matchesCorrect = lt.check(correctSentence, config).getMatches();
      if (isThereErrorAtPos(matchesCorrect, fromPos)) {
        results[j][classifyTypes.indexOf("FP")]++;
        if (verboseOutput) {
          System.out.println(ruleIds[j] + " FP: " + correctSentence);
        }
        // isFP = true;
      } else {
        results[j][classifyTypes.indexOf("TN")]++;
        // System.out.println(ruleIds[j] + " TN: " + correctSentence);
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
        System.out.println("Word cannot be replaced: " + wrongSentence);
        return;
      }

      List<RemoteRuleMatch> matchesWrong = lt.check(wrongSentence, config).getMatches();
      if (isThereErrorAtPos(matchesWrong, fromPos)) {
        results[1 - j][classifyTypes.indexOf("TP")]++;
        if (isExpectedSuggestionAtPos(matchesWrong, fromPos, words[j], wrongSentence, correctSentence)) {
          results[1 - j][classifyTypes.indexOf("TPs")]++;
        } else {
          if (verboseOutput) {
            System.out.println("TP without expected suggestion: " + wrongSentence);
          }
        }
      } else {
        results[1 - j][classifyTypes.indexOf("FN")]++;
        if (verboseOutput) {
          System.out.println(ruleIds[1 - j] + " FN: " + wrongSentence);
        }
        // isFN = true;
      }
    }
    // FP+FN in the same sentence -> probable error in corpus
    // if (isFP && isFN) {
    // System.out.println("POSSIBLE ERROR IN CORPUS: " + correctSentence);
    // }

  }

  private static boolean isThereErrorAtPos(List<RemoteRuleMatch> matchesCorrect, int pos) {
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
        return true;
      }
    }
    return false;
  }

  private static boolean isExpectedSuggestionAtPos(List<RemoteRuleMatch> matchesCorrect, int pos,
      String expectedSuggestion, String wrongSentence, String correctSentence) {
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
        for (String s : match.getReplacements().get()) {
          // the replacement rebuilds the original correct sentence
          String correctedSentence = wrongSentence.substring(0, match.getErrorOffset()) + s
              + wrongSentence.substring(match.getErrorOffset() + match.getErrorLength(), wrongSentence.length());
          if (correctedSentence.equals(correctSentence)) {
            return true;
          } else {
            return false;
          }
        }
      }
    }
    return false;
  }
}
