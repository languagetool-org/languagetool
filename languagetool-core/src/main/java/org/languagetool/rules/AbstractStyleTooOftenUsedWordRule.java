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
package org.languagetool.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.*;
import org.languagetool.rules.Category.Location;

/**
 * The method gives stylistic hints that a word is being used too often 
 * when the set percentage has been exceeded.(default off).
 * @author Fred Kruse
 * @since 6.2.3
 */
public abstract class AbstractStyleTooOftenUsedWordRule extends TextLevelRule {
  private static final Pattern OPENING_QUOTES = Pattern.compile("[\"“„»«]");
  private static final Pattern ENDING_QUOTES = Pattern.compile("[\"“”»«]");
  private static final boolean DEFAULT_ACTIVATION = false;
  private static final int MIN_WORD_COUNT= 100;

  private final int minPercent;
  private final int defaultMinPercent;
  private final Map<String, Integer> wordMap = new HashMap<>();

  private boolean withoutDirectSpeech = false;

  public AbstractStyleTooOftenUsedWordRule(ResourceBundle messages, Language lang, UserConfig userConfig, int minPercent) {
    this(messages, lang, userConfig, minPercent, DEFAULT_ACTIVATION);
  }

  public AbstractStyleTooOftenUsedWordRule(ResourceBundle messages, Language lang, UserConfig userConfig, int minPercent, boolean defaultActive) {
    super(messages);
    super.setCategory(new Category(new CategoryId("CREATIVE_WRITING"), 
        messages.getString("category_creative_writing"), Location.INTERNAL, false));
    if (!defaultActive) {
      setDefaultOff();
    }
    defaultMinPercent = minPercent;
    this.minPercent = getMinPercent(userConfig, minPercent);
    setLocQualityIssueType(ITSIssueType.Style);
  }

  /**
   * A token that has to be counted
   */
  protected abstract boolean isToCountedWord(AnalyzedTokenReadings token);
  
  /**
   * An exception is defined for the token
   */
  protected abstract boolean isException(AnalyzedTokenReadings token);
  
  /**
   * Gives back the lemma that should be added to the word map
   */
  protected abstract String toAddedLemma(AnalyzedTokenReadings token);
  
  /**
   * Defines the message for hints which exceed the limit
   */
  protected abstract String getLimitMessage(int minPercent);
  
  public abstract String getConfigureText();

  private int getMinPercent(UserConfig userConfig, int minPercentDefault) {
    if (userConfig != null) {
      Object[] cf = userConfig.getConfigValueByID(getId());
      if (cf != null) {
        return (int) cf[0];
      }
    }
    return minPercentDefault;
  }

  /**
   *  give the user the possibility to configure the function
   */
  @Override
  public RuleOption[] getRuleOptions() {
    RuleOption[] ruleOptions = { new RuleOption(defaultMinPercent, getConfigureText(), 1, 100) };
    return ruleOptions;
  }

  public Map<String, Integer> getWordMap() {
    return wordMap;
  }

  public void setWithoutDirectSpeech(boolean withoutDirectSpeech) {
    this.withoutDirectSpeech = withoutDirectSpeech;
  }
  
  /**
   * fill the map with all words and the number of occurrence
   */
  
  private void FillWordMap(List<AnalyzedSentence> sentences) {
    wordMap.clear();
    boolean excludeDirectSpeech = withoutDirectSpeech;
    boolean isDirectSpeech = false;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int n = 1; n < tokens.length; n++) {
        AnalyzedTokenReadings token = tokens[n];
        String sToken = token.getToken();
        if (excludeDirectSpeech && !isDirectSpeech && OPENING_QUOTES.matcher(sToken).matches() && n < tokens.length - 1 && !tokens[n + 1].isWhitespaceBefore()) {
          isDirectSpeech = true;
        } else if (excludeDirectSpeech && isDirectSpeech && ENDING_QUOTES.matcher(sToken).matches() && n > 1 && !tokens[n].isWhitespaceBefore()) {
          isDirectSpeech = false;
        } else if (!isDirectSpeech && !token.isWhitespace() && !token.isNonWord() &&
            isToCountedWord(token) && !isException(token)) {
          String lemma = toAddedLemma(token);
          if (lemma != null) {
            if (wordMap.containsKey(lemma)) {
              int num = wordMap.get(lemma) + 1;
              wordMap.put(lemma, num);
            } else {
              wordMap.put(lemma, 1);
            }
          }
        }
      }
    }
  }
  
  /**
   * get all words that are used more often than minPercent
   */
  private List<String> getTooOftenUsedWords() {
    List<String> words = new ArrayList<>();
    int numWords = 0;
    for (String word : wordMap.keySet()) {
      numWords += wordMap.get(word);
    }
    if (numWords < MIN_WORD_COUNT) {
      return words;
    }
    for (String word : wordMap.keySet()) {
      int percent = (int)((wordMap.get(word) * 100.) / (double) numWords);
      if (percent >= minPercent) {
        words.add(word);
      }
    }
    return words;
  }


  /* (non-Javadoc)
   * @see org.languagetool.rules.TextLevelRule#match(java.util.List)
   */
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    FillWordMap(sentences);
    List<String> tooOftenUsedWords = getTooOftenUsedWords();
    if (tooOftenUsedWords.size() < 1) {
      return toRuleMatchArray(ruleMatches);
    }
    int pos = 0;
    boolean excludeDirectSpeech = withoutDirectSpeech;
    boolean isDirectSpeech = false;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int n = 1; n < tokens.length; n++) {
        AnalyzedTokenReadings token = tokens[n];
        String sToken = token.getToken();
        if (excludeDirectSpeech && !isDirectSpeech && OPENING_QUOTES.matcher(sToken).matches() && n < tokens.length - 1 && !tokens[n + 1].isWhitespaceBefore()) {
          isDirectSpeech = true;
        } else if (excludeDirectSpeech && isDirectSpeech && ENDING_QUOTES.matcher(sToken).matches() && n > 1 && !tokens[n].isWhitespaceBefore()) {
          isDirectSpeech = false;
        } else if (!isDirectSpeech && !token.isWhitespace() && !token.isNonWord() &&
            isToCountedWord(token) && !isException(token)) {
          String lemma = toAddedLemma(token);
          if (lemma != null) {
            for (String word : tooOftenUsedWords) {
              if (lemma.equals(word)) {
                RuleMatch ruleMatch = new RuleMatch(this, sentence, token.getStartPos() + pos, token.getEndPos() + pos, 
                    getLimitMessage(minPercent));
                ruleMatches.add(ruleMatch);
                break;
              }
            }
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }
  
  @Override
  public int minToCheckParagraph() {
    return -1;
  }

  protected String getLemmaForPosTagStartsWith(String startPos, AnalyzedTokenReadings token) {
    List<AnalyzedToken> readings = token.getReadings();
    for (AnalyzedToken reading : readings) {
      String posTag = reading.getPOSTag();
      if (posTag != null && posTag.startsWith(startPos)) {
        return reading.getLemma();
      }
    }
    return null;
  }

}
