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

 import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
 import com.hankcs.hanlp.seg.CRF.CRFSegment;
 import com.hankcs.hanlp.seg.Segment;
 import com.hankcs.hanlp.seg.common.Term;

 import edu.berkeley.nlp.lm.NgramLanguageModel;
 import edu.berkeley.nlp.lm.io.LmReaders;

 import org.languagetool.AnalyzedSentence;
 import org.languagetool.JLanguageTool;
 import org.languagetool.databroker.ResourceDataBroker;
 import org.languagetool.language.SimplifiedChinese;
 import org.languagetool.rules.Rule;
 import org.languagetool.rules.RuleMatch;

 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;


 public class ChineseNgramProbabilityRule extends Rule {

   private RuleHelper ruleHelper = new RuleHelper();

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
     List<RuleMatch> ruleMatches = new ArrayList<>();
     String ss = sentence.getText();
     String maxProbSentence = ss;

     double score = ruleHelper.scoreSentence(ss);
     List<Term> tokens = ruleHelper.seg.seg(ss);
     int startPos = 0;

     for (Term t : tokens) {
       if (CoreStopWordDictionary.contains(t.word)) {
         startPos += t.word.length();
         continue;
       }
       if (t.length() <= 3) {
         for (int i = 0; i < t.word.length(); i++) {
           String currChar = Character.toString(t.word.charAt(i));
           List<String> newChars = ruleHelper.getCharReplacements(currChar);
           if (newChars == null) continue;
           for (String newChar : newChars) {
             if (ruleHelper.getFrequency(newChar) <= -6.5) continue;
             String newSentence = maxProbSentence.substring(0, startPos + i) + newChar + maxProbSentence.substring(startPos + i + 1);
             double newScore = ruleHelper.scoreSentence(newSentence);
             if (newScore > score) {
               maxProbSentence = newSentence;
               score = newScore;
             }
           }
         }
       }
       startPos += t.word.length();
     }

     for (int i = 0; i < ss.length(); i++) {
       String oldChar = Character.toString(ss.charAt(i));
       String newChar = Character.toString(maxProbSentence.charAt(i));
       if (!oldChar.equals(newChar)) {
         RuleMatch ruleMatch = new RuleMatch(this, sentence, i, i + 1, "");
         ruleMatch.setSuggestedReplacement(newChar);
         ruleMatches.add(ruleMatch);
       }
     }

     return toRuleMatchArray(ruleMatches);
   }

   private class RuleHelper {

     private Map<String, List<String>> similarDictionary;
     private LuceneLM trigram;
     private NgramLanguageModel<String> unigram;
     private Segment seg;

     private RuleHelper() {

       ResourceDataBroker resourceDataBroker = JLanguageTool.getDataBroker();
       trigram = new LuceneLM(new File("C:\\Dev\\ngramDemo\\data\\test\\index"));
//       System.out.println("Loaded Trigram.....");

       unigram = LmReaders.readLmBinary(resourceDataBroker.getFromResourceDirAsUrl("zh/char_unigram.binary").getPath());
//       System.out.println("Loaded Unigram.....");

       seg = new CRFSegment();
       seg.enablePartOfSpeechTagging(true);
       seg.enableOffset(true);
//       System.out.println("Enabled CRF segment.....");

       similarDictionary = new HashMap<>();
       try (BufferedReader br = new BufferedReader(new FileReader(resourceDataBroker.getFromResourceDirAsUrl("zh/similar_char_dictionary.txt").getPath()))) {
         String line;
         while ((line = br.readLine()) != null) {
           try {
             String[] kv = line.split(" ");
             String key = kv[0];
             List<String> val = Arrays.asList(kv[1].split(","));
             similarDictionary.put(key, val);
           } catch (Exception e) {
             System.out.print(String.format("%s format error.", line));
           }
         }
//         System.out.println("Loaded Similar Dictionary.....");
       } catch (IOException e) {
         System.out.println("Failed to similar_char_dictionary.");
       }
     }

     private double scoreSentence(String sentence) {
       List<Term> termList = seg.seg(sentence);
       List<String> ngrams = new ArrayList<>();
       for (Term t : termList) {
         ngrams.add(t.word);
       }
       return trigram.scoreSentence(ngrams);
     }

     private List<String> getCharReplacements(String character) {
       return similarDictionary.get(character);
     }

     private float getFrequency(String character) {
       return unigram.getLogProb(Arrays.asList(character));
     }
   }

   public static void main(String[] args) throws IOException {
     ChineseNgramProbabilityRule rule = new ChineseNgramProbabilityRule();
     JLanguageTool languageTool = new JLanguageTool(new SimplifiedChinese());
     String ss = "假书抵万金。";
     AnalyzedSentence sentence = languageTool.getAnalyzedSentence(ss);
     RuleMatch[] ruleMatches = rule.match(sentence);
     for (RuleMatch r : ruleMatches) {
       System.out.println(r.toString() + r.getSuggestedReplacements());
     }
   }
 }