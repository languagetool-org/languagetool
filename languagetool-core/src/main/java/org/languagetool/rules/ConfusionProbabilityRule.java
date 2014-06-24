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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.languagemodel.LanguageModel;

import java.io.*;
import java.util.*;

/**
 * WORK IN PROGRESS. LanguageTool's version of After the Deadline's homophone confusion check.
 */
public abstract class ConfusionProbabilityRule extends Rule {

  @Override
  public abstract String getDescription();

  private static final String HOMOPHONES = "homophonedb.txt";
  
  private final Map<String,ConfusionSet> wordToSet;
  private final LanguageModel languageModel;

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    super(messages);
    ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
    InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(HOMOPHONES);
    wordToSet = confusionSetLoader.loadConfusionSet(inputStream);
    this.languageModel = languageModel;
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
    String next = get(tokens, pos+1);
    String next2 = get(tokens, pos+2);
    String prev = get(tokens, pos-1);
    String prev2 = get(tokens, pos-2);
    @SuppressWarnings("UnnecessaryLocalVariable")
    double textScore = score(token.getToken(), next, next2, prev, prev2);
    double bestScore = textScore;
    String betterAlternative = null;
    //System.out.println("CF " + confusionSet.set + ", textScore:" + textScore);
    for (String alternative : confusionSet.set) {
      if (alternative.equalsIgnoreCase(token.getToken())) {
        // this is the text variant, calculated above already...
        continue;
      }
      double alternativeScore = score(alternative, next, next2, prev, prev2);
      //System.out.println(alternative + " ===> " + alternativeScore);
      if (alternativeScore > bestScore) {
        betterAlternative = alternative;
        bestScore = alternativeScore;
      }
    }
    return betterAlternative;
  }

  private String get(AnalyzedTokenReadings[] tokens, int i) {
    if (i == -1) {
      return "0BEGIN.0";  // TODO: this is the AtD marker
    } else if (i >= tokens.length) {
      return "0END.0";    // TODO: this is the AtD marker
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
    //System.out.println(prev + " " + option + " -> " + ngram1);
    //System.out.println(option + " " + next + " -> " + ngram2);
    
    // TODO: add a proper algorithm here that takes 1ngrams, 2grams and 3grams into account
    
    //return ngram1 + ngram2;
    return Math.max(1, ngram1) * Math.max(1, ngram2);
  }

  @Override
  public void reset() {
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
