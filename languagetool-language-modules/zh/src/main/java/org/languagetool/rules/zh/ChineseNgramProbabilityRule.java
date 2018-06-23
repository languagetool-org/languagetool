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

 package org.languagetool.rules.zh;

 import com.google.common.collect.Lists;
 import edu.berkeley.nlp.lm.NgramLanguageModel;
 import edu.berkeley.nlp.lm.io.LmReaders;
 import org.languagetool.AnalyzedSentence;
 import org.languagetool.AnalyzedTokenReadings;
 import org.languagetool.JLanguageTool;
 import org.languagetool.databroker.ResourceDataBroker;
 import org.languagetool.language.SimplifiedChinese;
 import org.languagetool.rules.Rule;
 import org.languagetool.rules.RuleMatch;


 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;


 public class ChineseNgramProbabilityRule extends Rule {

   private static ResourceDataBroker resourceDataBroker = JLanguageTool.getDataBroker();
   private static final String BIGRAM_PATH = resourceDataBroker.getFromResourceDirAsUrl("zh/zhwikiBigram.binary").getPath();
   private static final String TRIGRAM_PATH = resourceDataBroker.getFromResourceDirAsUrl("zh/zhwikiTrigram.binary").getPath();

   private Detector detector;

   public ChineseNgramProbabilityRule() {
     detector = new Detector();
   }


   @Override
   public String getDescription() {
     return "A rule that makes use of ngram language model to analyze text.";
   }

   @Override
   public String getId() {
     return "ZH_NGRAM_RULE";
   }

   @Override
   public RuleMatch[] match(AnalyzedSentence sentence) {

     String ss = sentence.getText();
     AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

     List<Integer> outindices = detector.detect(ss);
     List<RuleMatch> ruleMatches = new ArrayList<>();
     if (outindices.size() > 0) {

       List<List<Integer>> outranges = new ArrayList<>();
       List<List<Integer>> segranges = new ArrayList<>();
       for (int i : outindices) {
         outranges.add(Arrays.asList(i, i+1));
       }
       for (AnalyzedTokenReadings t : tokens) {
         segranges.add(Arrays.asList(t.getStartPos(), t.getEndPos()));
       }

       List<List<Integer>> ranges = mergeRanges(getRanges(outranges, segranges));

       for (List<Integer> r : ranges) {
         int startPos = r.get(0);
         int endPos = r.get(1);
         String cgram = correct(ss, startPos, endPos);
         ss = ss.substring(0, startPos) + cgram + ss.substring(endPos);
         RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, "拼写错误");
         ruleMatch.setSuggestedReplacement(cgram);
         ruleMatches.add(ruleMatch);
       }

     }
     return toRuleMatchArray(ruleMatches);
   }

   private boolean overlap(List<Integer> l1, List<Integer> l2) {
     if (l1.get(0) < l2.get(0)) {
       return l1.get(1) > l2.get(0);
     } else if (l1.get(0).equals(l2.get(0))) {
       return true;
     } else {
       return l1.get(0) < l2.get(1);
     }
   }

   private List<List<Integer>> getRanges(List<List<Integer>> outranges, List<List<Integer>> segranges) {
     Set<List<Integer>> ranges = new HashSet<>();
     for (List<Integer> segrange : segranges) {
       for (List<Integer> outrange : outranges) {
         if (overlap(outrange, segrange)) {
           ranges.add(segrange);
         }
       }
     }
     return Lists.newArrayList(ranges);
   }

   private List<List<Integer>> mergeRanges(List<List<Integer>> ranges) {
     Collections.sort(ranges, Comparator.comparing(e -> e.get(0)));
     List<Integer> saved = ranges.get(0);
     List<List<Integer>> results = new ArrayList<>();
     for (List<Integer> r : ranges) {
       int startPos = r.get(0);
       int endPos = r.get(1);
       if (startPos <= saved.get(1)) {
         saved.set(1, Math.max(saved.get(1), endPos));
       } else {
         results.add(Arrays.asList(saved.get(0), saved.get(1)));
         saved.set(0, startPos);
         saved.set(1, endPos);
       }
     }
     results.add(saved);
     return results;
   }

   private String correct(String sentence, int startPos, int endPos) {
     return sentence.substring(startPos, endPos);
   }



   private class Detector {
     //TODO: implement this class. (Code Period 2)
     private NgramLanguageModel<String> bimodel;
     private NgramLanguageModel<String> trimodel;

     private Detector() {
       bimodel   = LmReaders.readLmBinary(BIGRAM_PATH);
       trimodel  = LmReaders.readLmBinary(TRIGRAM_PATH);
     }


     /**
      * Get potential error character indices from a sentences ss.
      */
     private List<Integer> detect(String ss) {

       List<List<Float>> havgNgramScores = new ArrayList<>();
       havgNgramScores.add(getAvgNgramScores(ss, 2));
       havgNgramScores.add(getAvgNgramScores(ss, 3));

       List<Float> charScores = new ArrayList<>();
       for (int i = 0; i < ss.length(); i++) {
         float score = 0;
         for (List<Float> avgNgramScores : havgNgramScores) {
           score += avgNgramScores.get(i);
         }
         score = score / 2;
         charScores.add(score);
       }

       return getMadBasedOutlier(charScores, 1.2f);
     }

     /**
      * Use median absolute deviation(MAD) to estimate outliers.
      * For more statistical theory: https://en.wikipedia.org/wiki/Median_absolute_deviation.
      */
     private List<Integer> getMadBasedOutlier(List<Float> charScores, float threshold) {
       List<Integer> outindices = new ArrayList<>();
       // Get the median of the scores.
       float median = getMedian(charScores);
       List<Float> deviations = new ArrayList<>();
       // Get the deviation of each number from median(the estimation of the standard deviation).
       for (float charScore : charScores) {
         float d = (float) Math.sqrt(Math.pow((charScore - median), 2));
         deviations.add(d);
       }
       // Get MAD of the scores.
       float mad = getMedian(deviations);
       // Detect outliers.
       for (int i = 0; i < charScores.size(); i++) {
         double rate = 0.6745 * deviations.get(i) / mad;
         if (rate > threshold && charScores.get(i) < median) {
           outindices.add(i);
         }
       }
       return outindices;
     }

     /**
      * Calculate the median from samples.
      */
     private float getMedian(List<Float> numbers) {
       float median;
       float[] numArray = new float[numbers.size()];
       for (int i = 0; i < numbers.size(); i++) {
         numArray[i] = numbers.get(i);
       }
       Arrays.sort(numArray);
       if (numArray.length % 2 == 0) {
         median = (numArray[numArray.length/2] + numArray[numArray.length-1]) / 2;
       } else {
         median = numArray[numArray.length/2];
       }
       return median;
     }

     /**
      * Get char scores by averaging its adjacent ngram scores.
      * e.g.
      * ss - 我是中国人
      * the score of "是" for bigram is (P(是|我) + P(中|是)) / 2
      */
     private List<Float> getAvgNgramScores(String ss, int n) {
       List<Float> avgNgramScores = new ArrayList<>();

       List<Float> ngramScores = getNgramScores(ss, n);
       for (int i = 0; i < ss.length(); i++) {
         float score = 0;
         for (int j = 0; j < n; j++) {
           score += ngramScores.get(i+j);
         }
         avgNgramScores.add(score / n);
       }

       return avgNgramScores;
     }

     /**
      * Get ngram scores from a sentence ss
      * @param ss A raw sentence, e.g.我是中国人
      * @param n  "N" in Ngram
      * @return A list of ngram scores in order
      * e.g. { P(我|<s>), P(是|我), P(中|是)， P(国|中), P(人|国), P(人|</s>) }
      */
     private List<Float> getNgramScores(String ss, int n) {
       List<String> sentence = new ArrayList<>();
       // Preprocess sentence, i.e. add <s> and </s> symbols to the sentence.
       for (int i = 0; i < n-1; i++) {
         sentence.add("<s>");
       }
       sentence.addAll(Arrays.asList(ss.split("")));
       sentence.add("</s>");

       List<Float> scores = new ArrayList<>();
       for (int i = 0; i < sentence.size() - n + 1; i++) {
         List<String> ngram = sentence.subList(i, i+n);
         float score = getProb(ngram, n);
         scores.add(score);
       }

       if (n == 3) {
         scores.add(scores.get(scores.size() - 1));
       }

       return scores;
     }

     private float getProb(List<String> ngram, int k) {
       switch(k) {
         case 2 :
           return bimodel.getLogProb(ngram);
         case 3 :
           return trimodel.getLogProb(ngram);
         default :
           return bimodel.getLogProb(ngram);
       }
     }
   }




   public static void main(String[] args) throws IOException {

     /*
      * Test for mergeRanges
      int[][] outranges = {{0,1}, {1,2}, {2,3}, {3,4}, {6,7}, {7,8}};
      int[][] segranges = {{0, 2}, {2, 4}, {4, 5}, {5, 7}, {7, 8}, {8, 10}, {10, 11}, {11, 12}, {12, 13}, {13, 14}, {14, 16}, {16, 18}, {18, 19}, {19, 20}, {20, 22}, {22, 23}, {23, 24}, {24, 26}, {26, 27}, {27, 28}, {28, 30}, {30, 31}};
      List<List<Integer>> out = new ArrayList<>();
      for (int i = 0; i < outranges.length; i++) {
      List<Integer> range = new ArrayList<>();
      range.add(outranges[i][0]);
      range.add(outranges[i][1]);
      out.add(range);
      }
      List<List<Integer>> seg = new ArrayList<>();
      for (int i = 0; i < segranges.length; i++) {
      List<Integer> range = new ArrayList<>();
      range.add(segranges[i][0]);
      range.add(segranges[i][1]);
      seg.add(range);
      }
      System.out.println(out + "\n" + seg);
      System.out.println(rule.corrector.mergeRanges(rule.corrector.getRanges(out, seg)));
      */

     ChineseNgramProbabilityRule rule = new ChineseNgramProbabilityRule();
     JLanguageTool languageTool = new JLanguageTool(new SimplifiedChinese());

     String sighan = resourceDataBroker.getFromResourceDirAsUrl("zh/sighan15-A2-Training.txt").getPath();
     try (BufferedReader br = new BufferedReader(new FileReader(sighan))) {
       String line;
       while ((line =  br.readLine()) != null) {
         String[] l = line.split(" ");

         String ss = l[0]; // Raw sentences
         List<Integer> goldIndices = new ArrayList<>(); // Gold outliers
         for (String i : l[1].split(",")) {
           goldIndices.add(Integer.parseInt(i));
         }

         List<Integer> sysIndices = rule.detector.detect(ss); // System outliers

//         System.out.println(ss + "system:" + sysIndices + " gold:" + goldIndices);
         AnalyzedSentence sentence = languageTool.getAnalyzedSentence(ss);
         RuleMatch[] ruleMatches = rule.match(sentence);
         for (RuleMatch r : ruleMatches) {
           int from = r.getFromPos();
           int to = r.getToPos();
//           System.out.print(from + "-" + to + ":" + ss.substring(from, to) + " ");
           for (int i : goldIndices) {

           }
         }
         System.out.println();
       }
     }
   }
 }
