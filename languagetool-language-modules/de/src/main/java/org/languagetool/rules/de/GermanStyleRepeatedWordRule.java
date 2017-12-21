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
package org.languagetool.rules.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

/**
 * A rule checks the appearance of same words in a sentence or in two consecutive sentences.
 * Only substantive, verbs and adjectives are checked.
 * This rule detects no grammatic error but a stylistic problem (default off)
 * @author Fred Kruse
 */

public class GermanStyleRepeatedWordRule  extends TextLevelRule {
  
  private static final int MAX_TOKEN_TO_CHECK = 5;

  public GermanStyleRepeatedWordRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    setDefaultOff();
//    addExamplePair(Example.wrong("Der alte Mann wohnte in einem <marker>großen</marker> Haus. Es stand in einem <marker>großen</marker> Garten."),
//        Example.fixed("Der alte Mann wohnte in einem <marker>großen</marker> Haus. Es stand in einem <marker>weitläufigen</marker> Garten."));
  }

  @Override
  public String getId() {
    return "GERMAN_STYLE_REPEATED_WORD_RULE";
  }

  @Override
  public String getDescription() {
    return "Wiederholende Worte in aufeinanderfolgenden Sätzen";
  }
  
  /*
   * Only substantive, names, verbs and adjectives are checked
   */
  private static boolean isTokenToCheck(AnalyzedTokenReadings token) {
    return (token.matchesPosTagRegex("(SUB|EIG|VER|ADJ):.*") && !token.matchesPosTagRegex("ART:.*|ADV:.*|VER:(AUX|MOD):.*"));
  }
  
  /*
   * Pairs of substantive are excluded like "Arm in Arm", "Seite an Seite", etc.
   */
  private static boolean isTokenPair(AnalyzedTokenReadings[] tokens, int n, boolean before) {
    if(before) {
      if (tokens[n-2].matchesPosTagRegex("SUB:.*") && tokens[n-1].matchesPosTagRegex("PRP:.*") 
          && tokens[n].matchesPosTagRegex("SUB:.*")) {
        return true;
      }
    } else {
      if (tokens[n].matchesPosTagRegex("SUB:.*") && tokens[n+1].matchesPosTagRegex("PRP:.*") 
          && tokens[n+2].matchesPosTagRegex("SUB:.*")) {
        return true;
      }
    }
    return false;
  }
  
  /*
   * listings are excluded
   */
  private static boolean hasBreakToken(AnalyzedTokenReadings[] tokens) {
    for(int i = 0; i < tokens.length && i < MAX_TOKEN_TO_CHECK; i++) {
      if(tokens[i].getToken().equals("-") || tokens[i].getToken().equals("—") || tokens[i].getToken().equals("–")) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean isTokenInSentence(AnalyzedTokenReadings testToken, AnalyzedTokenReadings[] tokens) {
    return isTokenInSentence(testToken, tokens, -1);
  }
  
  /* 
   *  true if token is found in sentence
   */
  private static boolean isTokenInSentence(AnalyzedTokenReadings testToken, AnalyzedTokenReadings[] tokens, int notCheck) {
    if (testToken != null && tokens != null) {
      List<AnalyzedToken> readings = testToken.getReadings();
      if (readings.size() < 1) {
        return false;
      }
      String testBase = readings.get(0).getLemma();
      if (testBase == null) {
        return false;
      }
      for (int i = 0; i < tokens.length; i++) {
        if (i != notCheck && isTokenToCheck(tokens[i])) {
          readings = tokens[i].getReadings();
          if (readings.size() > 0) {
            String base = readings.get(0).getLemma();
            if (base != null) {
              if(testBase.equals(base)) {
                if(notCheck >= 0) {
                  if(notCheck == i - 2) {
                    return !isTokenPair(tokens, i, true);
                  } else if(notCheck == i + 2) {
                    return !isTokenPair(tokens, i, false);
                  } else if((notCheck == i + 1 || notCheck == i - 1) 
                      && testToken.getToken().equals(tokens[i].getToken())) {
                    return false;
                  }
                }
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      AnalyzedSentence lastSentence = null;
      AnalyzedSentence nextSentence = null;
      if (n > 0) {
        lastSentence = sentences.get(n - 1);
      }
      if (n < sentences.size() - 1) {
        nextSentence = sentences.get(n + 1);
      }
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      if(!hasBreakToken(tokens)) {
        AnalyzedTokenReadings[] lastTokens = null;
        AnalyzedTokenReadings[] nextTokens = null;
        if (lastSentence != null) {
          lastTokens = lastSentence.getTokensWithoutWhitespace();
        }
        if (nextSentence != null) {
          nextTokens = nextSentence.getTokensWithoutWhitespace();
        }
        for (int i = 0; i < tokens.length; i++) {
          AnalyzedTokenReadings token = tokens[i];
          if (isTokenToCheck(token)) {
            int isRepeated = 0;
            if (isTokenInSentence(token, tokens, i)) {
              isRepeated = 1;
            }
            if (isRepeated == 0 && lastTokens != null && isTokenInSentence(token, lastTokens)) {
              isRepeated = 2;
            }
            if (isRepeated == 0 && nextTokens != null && isTokenInSentence(token, nextTokens)) {
              isRepeated = 3;
            }
            if (isRepeated != 0) {
              String msg;
              if (isRepeated == 1) {
                msg = "Stilproblem: Das Wort wird bereits im selben Satz verwendet!";
              } else if (isRepeated == 2) {
                msg = "Stilproblem: Das Wort wird bereits im vorhergehenden Satz verwendet!";
              } else {
                msg = "Stilproblem: Das Wort wird bereits im nachfolgenden Satz verwendet!";
              }
              int startPos = pos + token.getStartPos();
              int endPos = pos + token.getEndPos();
              RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, msg);
              ruleMatches.add(ruleMatch);
            }
          } 
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

}
