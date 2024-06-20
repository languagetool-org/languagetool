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
package org.languagetool.openoffice.aisupport;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Tag;
import org.languagetool.openoffice.LinguisticServices;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.OfficeTools;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.RuleMatch.Type;
import org.languagetool.rules.TextLevelRule;

import com.sun.star.lang.Locale;

/**
 * Rule to detect errors by a AI API
 * @since 6.5
 * @author Fred Kruse
 */
public class AiDetectionRule extends TextLevelRule {

  public static final String RULE_ID = "LO_AI_DETECTION_RULE";
  public static final Color RULE_HINT_COLOR = new Color(90, 0, 255);
  public static final Color RULE_OTHER_COLOR = new Color(150, 150, 0);
  private static final Pattern QUOTES = Pattern.compile("[\"“”„»«]");
  private static final Pattern SINGLE_QUOTES = Pattern.compile("[‚‘’'›‹]");
  private static final Pattern PUNCTUATION = Pattern.compile("[,.!?:]");
  private static final Pattern OPENING_BRACKETS = Pattern.compile("[{(\\[]");

  private boolean debugMode = OfficeTools.DEBUG_MODE_AI;   //  should be false except for testing

  private final ResourceBundle messages;
  private final String aiResultText;
  private final String paraText;
  private final List<AnalyzedSentence> analyzedAiResult;
  private final String ruleMessage;
  private final boolean showStylisticHints;
  private final LinguisticServices linguServices;
  private final Locale locale;

  
  AiDetectionRule(String aiResultText, String paraText, List<AnalyzedSentence> analyzedAiResult, 
      LinguisticServices linguServices, Locale locale, ResourceBundle messages, boolean showStylisticHints) {
    this.aiResultText = aiResultText;
    this.analyzedAiResult = analyzedAiResult;
    this.messages = messages;
    this.paraText = paraText;
    this.showStylisticHints = showStylisticHints;
    this.linguServices = linguServices;
    this.locale = locale;
    ruleMessage = messages.getString("loAiRuleMessage");
    
    setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    setTags(Collections.singletonList(Tag.picky));
    
    if (debugMode) {
      MessageHandler.printToLogFile("AiDetectionRule: showStylisticHints: " + showStylisticHints);
    }


  }
  
  private boolean isIgnoredToken(String paraToken, String resultToken) {
/*
    if (resultToken.equals("\"")) {
      return QUOTES.matcher(paraToken).matches();
    }
    if (resultToken.equals("'")) {
      return SINGLE_QUOTES.matcher(paraToken).matches();
    }
*//*
    if (resultToken.equals("\"") || resultToken.equals("'")) {
      return QUOTES.matcher(paraToken).matches() || SINGLE_QUOTES.matcher(paraToken).matches();
    }
*/
    if (QUOTES.matcher(resultToken).matches() || SINGLE_QUOTES.matcher(resultToken).matches()) {
      return QUOTES.matcher(paraToken).matches() || SINGLE_QUOTES.matcher(paraToken).matches();
    }
    if (resultToken.equals("-")) {
      return paraToken.equals("–");
    }
    return false;
  }

  private boolean isQuote(String token) {
    return (QUOTES.matcher(token).matches() || SINGLE_QUOTES.matcher(token).matches());
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    List<AiRuleMatch> tmpMatches = new ArrayList<>();
    List<AiToken> paraTokens = new ArrayList<>();
    List<Integer> sentenceEnds = new ArrayList<>();
    int nSenTokens = 0;
    int nSentence = 0;
//    int lastSentenceStart = 0;
    int lastResultStart = 0;
    int pos = 0;
    int sEnd = 0;
    int sugStart = 0;
    int sugEnd = 0;
    boolean mergeSentences = false;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 1; i < tokens.length; i++) {
        paraTokens.add(new AiToken(tokens[i].getToken(), tokens[i].getStartPos() + pos, sentence, tokens[i].isNonWord()));
        sEnd++;
      }
      pos += sentence.getCorrectedTextLength();
      sentenceEnds.add(sEnd);
    }
    List<AiToken> resultTokens = new ArrayList<>();
    pos = 0;
    for (AnalyzedSentence sentence : analyzedAiResult) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 1; i < tokens.length; i++) {
        resultTokens.add(new AiToken(tokens[i].getToken(), tokens[i].getStartPos() + pos, null, tokens[i].isNonWord()));
      }
      pos += sentence.getCorrectedTextLength();
    }
    int i;
    int j = 0;
    for (i = 0; i < paraTokens.size() && j < resultTokens.size(); i++) {
      if (!paraTokens.get(i).token.equals(resultTokens.get(j).token) 
          && !isIgnoredToken(paraTokens.get(i).token, resultTokens.get(j).token)) {
        if ((i == 0 && ("{".equals(resultTokens.get(j).token) || "\"".equals(resultTokens.get(j).token)))
            && j + 1 < resultTokens.size() && paraTokens.get(i).token.equals(resultTokens.get(j + 1).token)) {
          j += 2;
          continue;
        }
        if (isQuote(paraTokens.get(i).token)
            && i + 1 < paraTokens.size() && paraTokens.get(i + 1).token.equals(resultTokens.get(j).token)) {
          continue;
        }
        int posStart = paraTokens.get(i).startPos;
        AnalyzedSentence sentence = paraTokens.get(i).sentence;
        int posEnd = 0;
        String suggestion = null;
        AiToken singleWordToken = null;
        boolean endFound = false;
        for (int n = 1; !endFound && i + n < paraTokens.size() && j + n < resultTokens.size(); n++) {
          for (int i1 = i + n; !endFound && i1 >= i; i1--) {
            for(int j1 = j + n; j1 >= j; j1--) {
              if (paraTokens.get(i1).token.equals(resultTokens.get(j1).token) 
                  || isIgnoredToken(paraTokens.get(i1).token, resultTokens.get(j1).token)) {
                endFound = true;
                if (i1 - 1 < i) {
                  if (i > 0) {
                    posStart = paraTokens.get(i - 1).startPos;
                    posEnd = paraTokens.get(i1 - 1).endPos;
                    sugStart = resultTokens.get(j - 1).startPos;
                    sugEnd = resultTokens.get(j1 - 1).endPos;
                    singleWordToken = j == j1 ? resultTokens.get(j1 - 1) : null;
                  } else {
                    posEnd = paraTokens.get(i1).endPos;
                    if (j < 1) {
                      j = 1;
                    }
                    if (j1 < 0) {
                      j1 = 0;
                    }
                    sugStart = resultTokens.get(j - 1).startPos;
                    sugEnd = resultTokens.get(j1).endPos;
                    singleWordToken = j - 1 == j1 ? resultTokens.get(j1) : null;
                  }
                } else {
                  posEnd = paraTokens.get(i1 - 1).endPos;
                  if (j < 0) {
                    j = 0;
                  }
                  if (j1 < 1) {
                    j1 = 1;
                  }
                  if (j <= j1 - 1) {
                    sugStart = resultTokens.get(j).startPos;
                    sugEnd = resultTokens.get(j1 - 1).endPos;
                    singleWordToken = j == j1 - 1 ? resultTokens.get(j) : null;
                  } else {
                    if (i > 0 && !PUNCTUATION.matcher(paraTokens.get(i - 1).token).matches()) {
                      posStart = paraTokens.get(i - 1).endPos;
                    } else {
                      posEnd = paraTokens.get(i1).startPos;
                    }
                    sugStart = resultTokens.get(j1).endPos;
                    sugEnd = resultTokens.get(j1).endPos;
                  }
                }
                nSenTokens += (i1 - i + 1);
                j = j1;
                i = i1;
                break;
              }
            }
          }
        }
        if (!endFound) {
          posEnd = paraTokens.get(paraTokens.size() - 1).endPos;
          sugStart = resultTokens.get(j).startPos;
          sugEnd = resultTokens.get(resultTokens.size() - 1).endPos;
          j = resultTokens.size() - 1;
        }
        suggestion = sugStart >= sugEnd ? "" : aiResultText.substring(sugStart, sugEnd);
        if (suggestion.isEmpty() || singleWordToken == null || singleWordToken.isNonWord
            || linguServices.isCorrectSpell(suggestion, locale)) {
          RuleMatch ruleMatch = new RuleMatch(this, sentence, posStart, posEnd, ruleMessage);
          ruleMatch.addSuggestedReplacement(suggestion);
          ruleMatch.setType(Type.Hint);
          tmpMatches.add(new AiRuleMatch(ruleMatch, sugStart, sugEnd));
          if (debugMode) {
            MessageHandler.printToLogFile("AiDetectionRule: match: found: start: " + posStart + ", end: " + posEnd
                + ", suggestion: " + suggestion);
          }
        } else if (debugMode) {
          MessageHandler.printToLogFile("AiDetectionRule: match: not correct spell: locale: " + OfficeTools.localeToString(locale)
              + ", suggestion: " + suggestion);
        }
      }
      j++;
      if (i >= sentenceEnds.get(nSentence)) {
        mergeSentences = true;
        while (i >= sentenceEnds.get(nSentence)) {
          nSentence++;
        }
      }
      if (i == sentenceEnds.get(nSentence) - 1) {
        if (nSenTokens > 0) {
          int allSenTokens = nSentence == 0 ? sentenceEnds.get(nSentence) : sentenceEnds.get(nSentence) - sentenceEnds.get(nSentence - 1);
          if (mergeSentences || nSenTokens > allSenTokens / 2) {
            if (showStylisticHints) {
              int startPos = tmpMatches.get(0).ruleMatch.getFromPos();
              int endPos = tmpMatches.get(tmpMatches.size() - 1).ruleMatch.getToPos();
              RuleMatch ruleMatch = new RuleMatch(this, null, startPos, endPos, ruleMessage);
              int suggestionStart = tmpMatches.get(0).suggestionStart;
              int suggestionEnd = tmpMatches.get(tmpMatches.size() - 1).suggestionEnd;
              String suggestion = aiResultText.substring(suggestionStart, suggestionEnd);
              ruleMatch.addSuggestedReplacement(suggestion);
              ruleMatch.setType(Type.Other);
              matches.add(ruleMatch);
              if (debugMode) {
                MessageHandler.printToLogFile("AiDetectionRule: match: Stylistic hint: suggestion: " + suggestion);
              }
            }
            mergeSentences = false;
          } else {
            addAllRuleMatches(matches, tmpMatches);
            if (debugMode) {
              MessageHandler.printToLogFile("AiDetectionRule: match: add matches: " + tmpMatches.size()
              + ", total: " + matches.size());
            }
          }
        }
        tmpMatches.clear();
        nSenTokens = 0;
        nSentence++;
//        lastSentenceStart = i + 1;
        lastResultStart = j;
      }
    }
    if (tmpMatches.size() > 0 || (j < resultTokens.size() && (!paraTokens.get(i - 1).token.equals(resultTokens.get(j - 1).token)
        || (!"}".equals(resultTokens.get(j).token) && !"\"".equals(resultTokens.get(j).token) 
            && !OPENING_BRACKETS.matcher(resultTokens.get(j).token).matches())))) {
      nSenTokens++;
      if (nSentence > 0) {
        nSentence--;
      }
      int allSenTokens = nSentence == 0 ? sentenceEnds.get(nSentence) : sentenceEnds.get(nSentence) - sentenceEnds.get(nSentence - 1);
      if (debugMode) {
        MessageHandler.printToLogFile("AiDetectionRule: match: j < resultTokens.size(): mergeSentences: " + mergeSentences
            + ", nSenTokens: " + nSenTokens + ", allSenTokens: " + allSenTokens);
      }
      if (mergeSentences || nSenTokens > allSenTokens / 2) {
        if (showStylisticHints) {
          int startPos;
          int endPos;
          int suggestionStart;
          int suggestionEnd;
          if (tmpMatches.size() > 0) {
            startPos = tmpMatches.get(0).ruleMatch.getFromPos();
            endPos = tmpMatches.get(tmpMatches.size() - 1).ruleMatch.getToPos();
            suggestionStart = tmpMatches.get(0).suggestionStart;
            suggestionEnd = tmpMatches.get(tmpMatches.size() - 1).suggestionEnd;
          } else {
            startPos = nSentence == 0 ? paraTokens.get(0).startPos : paraTokens.get(sentenceEnds.get(nSentence - 1)).startPos;
            endPos = paraTokens.get(paraTokens.size() - 1).endPos;
            suggestionStart = resultTokens.get(lastResultStart).startPos;
            suggestionEnd = resultTokens.get(resultTokens.size() - 1).endPos;
          }
          RuleMatch ruleMatch = new RuleMatch(this, null, startPos, endPos, ruleMessage);
          String suggestion = aiResultText.substring(suggestionStart, suggestionEnd);
          ruleMatch.addSuggestedReplacement(suggestion);
          ruleMatch.setType(Type.Other);
          matches.add(ruleMatch);
          if (debugMode) {
            MessageHandler.printToLogFile("AiDetectionRule: match: Stylistic hint: suggestion: " + suggestion);
          }
        }
      } else {
        int j1;
        for (j1 = j + 1; j1 < resultTokens.size() && !OPENING_BRACKETS.matcher(resultTokens.get(j1).token).matches(); j1++);
        if (j1 > resultTokens.size()) {
          j1 = resultTokens.size();
        }
        String suggestion = aiResultText.substring(resultTokens.get(j - 1).startPos, resultTokens.get(j1 - 1).endPos);
        if (suggestion.isEmpty() || j != j1 || resultTokens.get(j - 1).isNonWord || linguServices.isCorrectSpell(suggestion, locale)) {
          RuleMatch ruleMatch = new RuleMatch(this, null, paraTokens.get(paraTokens.size() - 1).startPos, 
              paraTokens.get(paraTokens.size() - 1).endPos, ruleMessage);
          ruleMatch.addSuggestedReplacement(suggestion);
          ruleMatch.setType(Type.Hint);
          tmpMatches.add(new AiRuleMatch(ruleMatch, resultTokens.get(j - 1).startPos, resultTokens.get(resultTokens.size() - 1).endPos));
        }
        addAllRuleMatches(matches, tmpMatches);
        if (debugMode) {
          MessageHandler.printToLogFile("AiDetectionRule: match: add matches: " + tmpMatches.size()
          + ", total: " + matches.size());
        }
      }
    }
    if (debugMode && j < resultTokens.size()) {
      MessageHandler.printToLogFile("AiDetectionRule: match: j < resultTokens.size(): paraTokens.get(i - 1): " + paraTokens.get(i - 1).token
          + ", resultTokens.get(j - 1): " + resultTokens.get(j - 1).token);
    }
    return toRuleMatchArray(matches);
  }
  
  private void addAllRuleMatches (List<RuleMatch> matches, List<AiRuleMatch> aiMatches) {
    for (AiRuleMatch match : aiMatches) {
      matches.add(match.ruleMatch);
    }
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return messages.getString("loAiRuleDescription");
  }
  
  class AiRuleMatch {
    public final RuleMatch ruleMatch;
    public final int suggestionStart;
    public final int suggestionEnd;
    
    AiRuleMatch(RuleMatch ruleMatch, int suggestionStart, int suggestionEnd) {
      this.ruleMatch = ruleMatch;
      this.suggestionStart = suggestionStart;
      this.suggestionEnd = suggestionEnd;
    }
  }

}
