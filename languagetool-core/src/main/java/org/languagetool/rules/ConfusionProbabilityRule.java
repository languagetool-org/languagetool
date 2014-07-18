/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.PersistBasicNetwork;
import org.encog.persist.EncogPersistor;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.languagemodel.LanguageModel;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * LanguageTool's version of After the Deadline's homophone confusion check.
 */
public abstract class ConfusionProbabilityRule extends Rule {

  @Override
  public abstract String getDescription();

  private static final String HOMOPHONES = "homophonedb.txt";
  
  private final Map<String,ConfusionSet> wordToSet;
  private final LanguageModel languageModel;
  private final BasicNetwork network;
  
  private boolean debug;

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    this(messages, languageModel, null);
  }
  
  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, File networkFile) throws IOException {
    super(messages);
    ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
    InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(HOMOPHONES);
    wordToSet = confusionSetLoader.loadConfusionSet(inputStream);
    this.languageModel = languageModel;
    network = networkFile != null ? load(networkFile) : null;
  }

  private BasicNetwork load(File inputFile) throws IOException {
    EncogPersistor persistor = new PersistBasicNetwork();
    try (FileInputStream inputStream = new FileInputStream(inputFile)) {
      Object read = persistor.read(inputStream);
      return (BasicNetwork)read;
    }
  }

  /** @deprecated used only for tests */
  public void setConfusionSet(ConfusionSet set) {
    wordToSet.clear();
    for (String word : set.set) {
      wordToSet.put(word, set);
    }
  }

  @Override
  public String getId() {
    return "CONFUSION_RULE";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    List<RuleMatch> matches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedTokenReadings token : tokens) {
      ConfusionSet confusionSet = wordToSet.get(token.getToken());
      boolean isEasilyConfused = confusionSet != null;
      if (isEasilyConfused) {
        //System.out.println("*** isEasilyConfused:" + token.getToken());
        String betterAlternative = getBetterAlternativeOrNull(tokens, pos, confusionSet);
        if (betterAlternative != null) {
          int endPos = token.getStartPos() + token.getToken().length();
          RuleMatch match = new RuleMatch(this, token.getStartPos(), endPos, "Did you maybe mean '" + betterAlternative + "'?");
          match.setSuggestedReplacement(betterAlternative);
          matches.add(match);
        }
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  // non-private for tests
  String getBetterAlternativeOrNull(AnalyzedTokenReadings[] tokens, int pos, ConfusionSet confusionSet) {
    AnalyzedTokenReadings token = tokens[pos];
    //
    // TODO: LT's tokenization is different to the Google one. E.g. Google "don't" vs LT "don ' t"
    //
    String next = get(tokens, pos+1);
    String next2 = get(tokens, pos+2);
    String prev = get(tokens, pos-1);
    String prev2 = get(tokens, pos-2);
    @SuppressWarnings("UnnecessaryLocalVariable")
    //double textScore = score(token.getToken(), next, next2, prev, prev2) - 10_000;  // 47,66 f 0.5 measure 
    double textScore = score(token.getToken(), next, next2, prev, prev2) + 0;  // 48.97
    //double textScore = score(token.getToken(), next, next2, prev, prev2) + 25_000;  // 49,15
    //double textScore = score(token.getToken(), next, next2, prev, prev2) + 1_000_000;  // 48,82
    //double textScore = score(token.getToken(), next, next2, prev, prev2) + 10_000_000;  // 48,53
    //double textScore = score(token.getToken(), next, next2, prev, prev2) + 100_000_000;  // 48,01
    //double textScore = Double.MAX_VALUE; // 30,15
    double bestScore = textScore;
    String betterAlternative = null;
    NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
    format.setMinimumIntegerDigits(16);
    format.setMinimumFractionDigits(0);
    format.setGroupingUsed(false);
    if (debug) {
      System.out.println();
    }
    for (String alternative : confusionSet.set) {
      if (alternative.equalsIgnoreCase(token.getToken())) {
        // this is the text variant, calculated above already...
        if (debug) {
          System.out.println("  " + format.format(textScore) + ": " + alternative + " [input]");
        }
        continue;
      }
      double alternativeScore = score(alternative, next, next2, prev, prev2);
      if (debug) {
        System.out.println("  " + format.format(alternativeScore) + ": " + alternative);
      }
      if (alternativeScore > bestScore) {
        betterAlternative = alternative;
        bestScore = alternativeScore;
      }
    }
    if (debug) {
      System.out.println("  Result => " + betterAlternative);
    }
    return betterAlternative;
  }

  private String get(AnalyzedTokenReadings[] tokens, int i) {
    if (i == -1) {
      return LanguageModel.GOOGLE_SENTENCE_START;
    } else if (i >= tokens.length) {
      return LanguageModel.GOOGLE_SENTENCE_END;
    } else if (i >= 0 && i < tokens.length) {
      return tokens[i].getToken();
    }
    return null;
  }

  private double score(String option, String next, String next2, String prev, String prev2) {
    // See misuseFeatures in AtD's lib/spellcheck.sl:
    //misuseFeatures($current, $option, $options, $pre, $next, $tags, $pre2, $next2)]
    //..................1........2.........3.......4......5......6......7......8
    /*
      postf       => Pbigram2($option, $next), Pbigram2(“word”, “next”): This method calculates P(word|next) or the probability of the specified word given the next word
      pref        => Pbigram1($pre, $option), Pbigram1(“previous”, “word”): This method calculates P(word|previous) or the probability of the specified word given the previous word.
      probability => Pword($option),
      trigram     => Ptrigram($pre2, $pre, $option),
      trigram2    => Ptrigram2($option, $next, $next2)
      also see http://blog.afterthedeadline.com/2010/03/04/all-about-language-models/
    */
    //System.out.println("---------------------------");
    //System.out.println(option + ", next " + next + ", prev: " + prev);

    long ngram1 = languageModel.getCount(prev, option);
    long ngram2 = languageModel.getCount(option, next);
    long ngram3 = languageModel.getCount(prev, option, next);
    //System.out.printf("l:%d r:%d 3gram:%d (%s)\n", ngram1, ngram2, ngram3, prev + " " + option + " " + next);
    //long ngram3 = 1;
    
    // TODO: add a proper algorithm here that takes 1ngrams, 2grams and 3grams into account
    
    if (network != null) {
      double value = network.compute(new BasicMLData(new double[]{ngram1, ngram2})).getData(0);
      //System.out.println("***" + ngram1 + " - " + ngram2 + " => " + network.compute(new BasicMLData(new double[]{ngram1, ngram2})));
      //return value > 0.5 ? 1 : 0;
      return value;
    } else {
      // return 0.0*ngram1 + ngram2;  // 31,18% recall
      //return 0.2*ngram1 + ngram2;  // 31,41%
      //return 0.6*ngram1 + ngram2;  // 31,77%
      //return 1.0*ngram1 + ngram2;  // 31,89%
      //return 1.4*ngram1 + ngram2;  // 32,13%
      //return 5*ngram1 + ngram2;  // 32,25%
      //return 10*ngram1 + ngram2;  // 32,49%
      //return 20*ngram1 + ngram2;  // 32,61%
      //return 100*ngram1 + ngram2;  // 32,73%
      //return 500*ngram1 + ngram2;  // 32,97%
      //return 1000*ngram1 + ngram2;  // 33,09% <==
      //return 2000*ngram1 + ngram2;  // 32,97%
      //return 5000*ngram1 + ngram2;  // 32,85%
      //return ngram1;  // 30,70%

      // baseline:
      //double val = 1.0;  // f-measure: 0.305 (perfect suggestions only)
      
      // 2grams only:
      double val = Math.max(1, ngram1) * Math.max(1, ngram2);  // f-measure: 0.490 (perfect suggestions only)

      // 2grams and 3grams:
      //double val1 = Math.log(Math.max(1, ngram1));  // use Math.log to avoid huge number causing overflows
      //double val2 = Math.log(Math.max(1, ngram2));
      //double val3 = Math.log(Math.max(1, ngram3));
      //double val = val1 * val2 * val3;  // f-measure: 0.468 (perfect suggestions only)

      // 3grams only:
      //double val = Math.max(1, ngram3);  // f-measure: 0.473 (perfect suggestions only)

      //System.out.printf(option + ": %f %f %f => %f\n", val1, val2, val3, val);
      return val;
    }
  }

  @Override
  public void reset() {
  }

  protected void setDebug(boolean debug) {
    this.debug = debug;
  }

  public static class ConfusionSet {
    Set<String> set = new HashSet<>();

    ConfusionSet(String... words) {
      Collections.addAll(this.set, words);
    }
    
    public Set<String> getSet() {
      return set;
    }
  }
}
