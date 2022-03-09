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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.LinguServices;
import org.languagetool.UserConfig;

/**
 * An abstract rule checks the appearance of same words in a sentence or in two consecutive sentences.
 * The isTokenToCheck method can be used to check only specific words (e.g. substantive, verbs and adjectives).
 * This rule detects no grammar error but a stylistic problem (default off)
 * @author Fred Kruse
 * @since 4.1
 */
public abstract class AbstractStyleRepeatedWordRule  extends TextLevelRule {
  
  private static final int MAX_TOKEN_TO_CHECK = 5;
  
  protected final LinguServices linguServices;
  protected final Language lang;
  
  protected int maxDistanceOfSentences = 1;

  public AbstractStyleRepeatedWordRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    setDefaultOff();
    this.lang = lang;
    if (userConfig != null) {
      linguServices = userConfig.getLinguServices();
      int confDistance = userConfig.getConfigValueByID(getId());
      if (confDistance >= 0) {
        this.maxDistanceOfSentences = confDistance;
      }
    } else {
      linguServices = null;
    }
  }

  /**
   * Override this ID by adding a language acronym (e.g. STYLE_REPEATED_WORD_RULE_DE)
   * to use adjustment of maxWords by option panel
   * @since 4.1
   */   
  @Override
  public String getId() {
    return "STYLE_REPEATED_WORD_RULE";
  }

  @Override
  public String getDescription() {
    return "Repeated words in consecutive sentences";
  }
  
  /*
   * Message for repeated word in same sentence
   */
  protected abstract String messageSameSentence();
  
  /*
   * Message for repeated word in sentence before
   */
  protected abstract String messageSentenceBefore();
  
  /*
   * Message for repeated word in sentence after
   */
  protected abstract String messageSentenceAfter();
  
  /*
   * get maximal Distance of words in number of sentences
   * @since 4.1
   */
  @Override
  public int getDefaultValue() {
    return maxDistanceOfSentences;
  }
  
  /**
   * @since 4.2
   */
  @Override
  public boolean hasConfigurableValue() {
    return true;
  }

  /**
   * @since 4.2
   */
  @Override
  public int getMinConfigurableValue() {
    return 0;
  }

  /**
   * @since 4.2
   */
  @Override
  public int getMaxConfigurableValue() {
    return 5;
  }

  /**
   * @since 4.2
   */
  public String getConfigureText() {
    return messages.getString("guiStyleRepeatedWordText");
  }

  /*
   * Check only special words (e.g substantive, verbs, adjectives)
   * (German example: return (token.matchesPosTagRegex("(SUB|EIG|VER|ADJ):.*") 
   *              && !token.matchesPosTagRegex("ART:.*|ADV:.*|VER:(AUX|MOD):.*"));
   */
  protected abstract boolean isTokenToCheck(AnalyzedTokenReadings token);
    
  /*
   * Is checked word part of pairs like "arm in arm", "side by side", etc. (exclude such pairs)
   */
  protected abstract boolean isTokenPair(AnalyzedTokenReadings[] tokens, int n, boolean before);
  
  /*
   * listings are excluded
   */
  private static boolean hasBreakToken(AnalyzedTokenReadings[] tokens) {
    for (int i = 0; i < tokens.length && i < MAX_TOKEN_TO_CHECK; i++) {
      if (tokens[i].getToken().equals("-") || tokens[i].getToken().equals("—") || tokens[i].getToken().equals("–")) {
        return true;
      }
    }
    return false;
  }
  
  private boolean isTokenInSentence(AnalyzedTokenReadings testToken, AnalyzedTokenReadings[] tokens) {
    return isTokenInSentence(testToken, tokens, -1);
  }
  
  /* 
   *  true if token is part of composite word in sentence
   *  override for languages like German which contents composed words
   */
  protected boolean isPartOfWord(String testTokenText, String tokenText) {
    return false;
  }

  /* 
   *  true if is an exception of token pair
   *  note: method is called after two tokens are tested to share the same lemma
   */
  protected boolean isExceptionPair(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    return false;
  }

  /* 
   * Set a URL to a synonym dictionary for a token
   */
  protected URL setURL(AnalyzedTokenReadings token ) throws MalformedURLException {
    return null;
  }
  
  /**
   * get synonyms for a word
   */
  public List<String> getSynonymsForWord(String word) {
    List<String> synonyms = new ArrayList<String>();
    List<String> rawSynonyms = linguServices.getSynonyms(word, lang);
    for (String synonym : rawSynonyms) {
      synonym = synonym.replaceAll("\\(.*\\)", "").trim();
      if (!synonym.isEmpty() && !synonyms.contains(synonym)) {
        synonyms.add(synonym);
      }
    }
    return synonyms;
  }

  /**
   * get synonyms for a repeated word
   */
  public List<String> getSynonyms(AnalyzedTokenReadings token) {
    List<String> synonyms = new ArrayList<String>();
    if(linguServices == null || token == null) {
      return synonyms;
    }
    List<AnalyzedToken> readings = token.getReadings();
    for (AnalyzedToken reading : readings) {
      String lemma = reading.getLemma();
      if (lemma != null) {
        List<String> newSynonyms = getSynonymsForWord(lemma);
        for (String synonym : newSynonyms) {
          if (!synonyms.contains(synonym)) {
            synonyms.add(synonym);
          }
        }
      }
    }
    if(synonyms.isEmpty()) {
      synonyms = getSynonymsForWord(token.getToken());
    }
    return synonyms;
  }

  /* 
   *  true if token is found in sentence
   */
  private boolean isTokenInSentence(AnalyzedTokenReadings testToken, AnalyzedTokenReadings[] tokens, int notCheck) {
    if (testToken == null || tokens == null) {
      return false;
    }
    List<AnalyzedToken> readings = testToken.getReadings();
    List<String> lemmas = new ArrayList<>();
    for (AnalyzedToken reading : readings) {
      if (reading.getLemma() != null) {
        lemmas.add(reading.getLemma());
      }
    }
    for (int i = 0; i < tokens.length; i++) {
      if (i != notCheck && isTokenToCheck(tokens[i])) {
        if ((!lemmas.isEmpty() && tokens[i].hasAnyLemma(lemmas.toArray(new String[0])) && !isExceptionPair(testToken, tokens[i])) 
            || isPartOfWord(testToken.getToken(), tokens[i].getToken())) {
          if (notCheck >= 0) {
            if (notCheck == i - 2) {
              return !isTokenPair(tokens, i, true);
            } else if (notCheck == i + 2) {
              return !isTokenPair(tokens, i, false);
            } else if ((notCheck == i + 1 || notCheck == i - 1) 
                && testToken.getToken().equals(tokens[i].getToken())) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    List<AnalyzedTokenReadings[]> tokenList = new ArrayList<>();
    int pos = 0;
    for (int n = 0; n < maxDistanceOfSentences && n < sentences.size(); n++) {
      tokenList.add(sentences.get(n).getTokensWithoutWhitespace());
    }
    for (int n = 0; n < sentences.size(); n++) {
      if (n + maxDistanceOfSentences < sentences.size()) {
        tokenList.add(sentences.get(n + maxDistanceOfSentences).getTokensWithoutWhitespace());
      }
      if (tokenList.size() > 2 * maxDistanceOfSentences + 1) {
        tokenList.remove(0);
      }
      int nTok = maxDistanceOfSentences;
      if (n < maxDistanceOfSentences) {
        nTok = n;
      } else if (n >= sentences.size() - maxDistanceOfSentences) {
        nTok = tokenList.size() - (sentences.size() - n);
      }
      if (!hasBreakToken(tokenList.get(nTok))) {
        for (int i = 0; i < tokenList.get(nTok).length; i++) {
          AnalyzedTokenReadings token = tokenList.get(nTok)[i];
          if (isTokenToCheck(token)) {
            int isRepeated = 0;
            if (isTokenInSentence(token, tokenList.get(nTok), i)) {
              isRepeated = 1;
            }
            for(int j = nTok - 1; isRepeated == 0 && j >= 0 && j >= nTok - maxDistanceOfSentences; j--) {
              if (isTokenInSentence(token, tokenList.get(j))) {
                isRepeated = 2;
              }
            }
            for(int j = nTok + 1; isRepeated == 0 && j < tokenList.size() && j <= nTok + maxDistanceOfSentences; j++) {
              if (isTokenInSentence(token, tokenList.get(j))) {
                isRepeated = 3;
              }
            }
            if (isRepeated != 0) {
              String msg;
              if (isRepeated == 1) {
                msg = messageSameSentence();
              } else if (isRepeated == 2) {
                msg = messageSentenceBefore();
              } else {
                msg = messageSentenceAfter();
              }
              int startPos = pos + token.getStartPos();
              int endPos = pos + token.getEndPos();
              RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, msg);
              List<String> suggestions = getSynonyms(token);
              if(!suggestions.isEmpty()) {
                ruleMatch.setSuggestedReplacements(suggestions);
              }
              URL url = setURL(token);
              if(url != null) {
                ruleMatch.setUrl(url);
              }
              ruleMatches.add(ruleMatch);
            }
          } 
        }
      }
      pos += sentences.get(n).getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }
  
  @Override
  public int minToCheckParagraph() {
    return maxDistanceOfSentences;
  }
  
}
