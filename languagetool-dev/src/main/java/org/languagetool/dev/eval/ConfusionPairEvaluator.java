/* LanguageTool, a natural language style checker 
 * Copyright (C) 2022 Jaume Ortolà
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
package org.languagetool.dev.eval;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class ConfusionPairEvaluator {
  
  private final static String encoding = "UTF-8";
  static String[] words = new String[2];
  static String[] ruleIds = new String[2];
  static int[][] results = new int[2][4]; // word0/word1 ; TP/FP/TN/FN
  static Language lang = null;
  static JLanguageTool lt = null;
  static List<String> classifyTypes = Arrays.asList("TP", "FP", "TN", "FN");
    
  public static void main(String[] args) throws Exception {
    if (args.length != 6) {
      help();
    }
    long start = System.currentTimeMillis();
    lang = Languages.getLanguageForShortCode(args[0]);
    lt = new JLanguageTool(lang);
    String filename = args[1];
    words[0] = args[2];
    words[1] = args[3];
    ruleIds[0] = args[4];
    ruleIds[1] = args[5];
    for (Rule rule : lt.getAllRules()) {
      if (!rule.getId().equals(ruleIds[0]) && !rule.getId().equals(ruleIds[1])) {
        lt.disableRule(rule.getId());
      }
    }
    try (InputStreamReader isr = getInputStreamReader(filename, encoding);
        BufferedReader br = new BufferedReader(isr)) {
      String line;
      while ((line = br.readLine()) != null) {
        List<String> sentencesLine = lt.sentenceTokenize(line);
        for (String sentence : sentencesLine) {
          List<String> tokens = lang.getWordTokenizer().tokenize(sentence);
          int count0=0;
          int count1=0;
          for (String token : tokens) {
            if (token.equals(words[0])) {
              count0++;
            }
            if (token.equals(words[1])) {
              count1++;
            }
          }
          if (count0>0 && count1>0) {
            System.out.println("WARNING Sentence with the two words: " + sentence);
          } else if (count0==1 && count1==0) {
            analyzeSentence(sentence, 0);
          } else if (count0==0 && count1==1) {
            analyzeSentence(sentence, 1);
          } else if (count0>1 || count1>1) {
            System.out.println("WARNING Sentence with a repeated word: " + sentence);
          }
        }
      }
    }
    
    for (int i=0; i<2; i++) {
      System.out.println("Results for rule "+ruleIds[i]);
      for (int j=0; j<4; j++) {
        System.out.println(classifyTypes.get(j)+": "+results[i][j]);
      }
      float precision = results[i][classifyTypes.indexOf("TP")] / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FP")]);
      float recall = results[i][classifyTypes.indexOf("TP")] / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FN")]);
      System.out.println("Precision: " + String.format("%.4f", precision));
      System.out.println("Recall: " + String.format("%.4f", recall));
    }
    
    float time = (float) ((System.currentTimeMillis() - start) / 1000.0);
    System.out.println("Total time: " + String.format("%.2f", time) + " seconds");
  }
  
  private static void analyzeSentence(String correctSentence, int j) throws IOException {

    List<RuleMatch> matchesCorrect = lt.check(correctSentence);
    if (containsID(matchesCorrect, ruleIds[j])) {
      results[j][classifyTypes.indexOf("FP")]++;
      System.out.println(ruleIds[j] + " FP: " + correctSentence);
    } else {
      results[j][classifyTypes.indexOf("TN")]++;
      //System.out.println(ruleIds[j] + " TN: " + correctSentence);
    }

    String wrongSentence = correctSentence.replaceAll("\\b" + words[j] + "\\b", words[1 - j]);
    List<RuleMatch> matchesWrong = lt.check(wrongSentence);
    if (containsID(matchesWrong, ruleIds[1 - j])) {
      results[1 - j][classifyTypes.indexOf("TP")]++;
      //System.out.println(ruleIds[1 - j] + " TP: " + wrongSentence);
    } else {
      results[1 - j][classifyTypes.indexOf("FN")]++;
      System.out.println(ruleIds[1 - j] + " FN: " + wrongSentence);
    }
    
    //FP+FN in the same sentence -> probable error in corpus

  }
  
  private static boolean containsID (List<RuleMatch> matches, String id) {
    for (RuleMatch match : matches) {
      if (match.getRule().getId().equals(id)) {
        return true;
      }
    }
    return false;
  }
  
  private int indexOfWord(String word, String sentence) {
    Pattern p = Pattern.compile("\\b" + word + "\\b");
    Matcher m = p.matcher(sentence);
    if (m != null) {
      return m.start();
    }
    return -1;
  }

  private static InputStreamReader getInputStreamReader(String filename, String encoding) throws IOException {
    String charsetName = encoding != null ? encoding : Charset.defaultCharset().name();
    InputStream is = System.in;
    if (!isStdIn(filename)) {
      is = new FileInputStream(new File(filename));
      BOMInputStream bomIn = new BOMInputStream(is, true, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE,
          ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE);
      if (bomIn.hasBOM() && encoding == null) {
        charsetName = bomIn.getBOMCharsetName();
      }
      is = bomIn;
    }
    return new InputStreamReader(new BufferedInputStream(is), charsetName);
  }
  
  private static boolean isStdIn(String filename) {
    return "-".equals(filename);
  }
  
  private static void help() {
    System.out.println("Usage: " + ConfusionPairEvaluator.class.getSimpleName()
        + " <language code> <intput file> word1 word2 ruleId1 ruleId2");
    System.exit(1);
  }
  

}
