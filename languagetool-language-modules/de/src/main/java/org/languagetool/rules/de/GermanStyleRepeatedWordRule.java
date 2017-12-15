package org.languagetool.rules.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
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
  
  private static final int MAX_TOKEN_TO_CHECK = 4;

  public GermanStyleRepeatedWordRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    setDefaultOff();
  }

  @Override
  public String getId() {
    return "GERMAN_STYLE_REPEATED_WORD_RULE";
  }

  @Override
  public String getDescription() {
    return "Wiederholende Worte in aufeinanderfolgenden Sätzen";
  }
  
  private static boolean isTokenToCheck(AnalyzedTokenReadings token) {
    return (token.matchesPosTagRegex("(SUB|EIG|VER|ADJ):.*") && !token.matchesPosTagRegex("ART:.*|ADV:.*|VER:(AUX|MOD):.*"));
  }
  
  private static boolean hasBreakToken(AnalyzedTokenReadings[] tokens) {
    for(int i = 0; i < tokens.length && i < MAX_TOKEN_TO_CHECK; i++) {
      if(tokens[i].getToken().equals("-") || tokens[i].getToken().equals("–")) return true;
    }
    return false;
  }
  
  private static boolean isTokenInSentence(AnalyzedTokenReadings testToken, AnalyzedTokenReadings[] tokens) {
    return isTokenInSentence(testToken, tokens, -1);
  }
  
  private static boolean isTokenInSentence(AnalyzedTokenReadings testToken, AnalyzedTokenReadings[] tokens, int notCheck) {
    if (testToken != null && tokens != null) {
      List<AnalyzedToken> readings = testToken.getReadings();
      if (readings.size() < 1) return false;
      String testBase = readings.get(0).getLemma();
      if (testBase == null) return false;
      for (int i = 0; i < tokens.length; i++) {
        if (i != notCheck && isTokenToCheck(tokens[i])) {
          readings = tokens[i].getReadings();
          if (readings.size() > 0) {
            String base = readings.get(0).getLemma();
            if (base != null) {
              if(testBase.equals(base)) return true;
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
      if (n > 0) lastSentence = sentences.get(n - 1);
      if (n < sentences.size() - 1) nextSentence = sentences.get(n + 1);
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      if(!hasBreakToken(tokens)) {
        AnalyzedTokenReadings[] lastTokens = null;
        AnalyzedTokenReadings[] nextTokens = null;
        if (lastSentence != null) lastTokens = lastSentence.getTokens();
        if (nextSentence != null) nextTokens = nextSentence.getTokens();
        for (int i = 0; i < tokens.length; i++) {
          AnalyzedTokenReadings token = tokens[i];
          if (isTokenToCheck(token)) {
            boolean isRepeated = false;
            isRepeated = isTokenInSentence(token, tokens, i);
            if (!isRepeated && lastTokens != null) isRepeated = isTokenInSentence(token, lastTokens);
            if (!isRepeated && nextTokens != null) isRepeated = isTokenInSentence(token, nextTokens);
            if (isRepeated) {
              String msg = "Stilproblem: Wortwiederholung";
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
