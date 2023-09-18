/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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

package org.languagetool.rules.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.tools.StringTools;

public class CatalanUnpairedQuestionMarksRule extends TextLevelRule {

  public CatalanUnpairedQuestionMarksRule(ResourceBundle messages, Language language) {
    super();
    setLocQualityIssueType(ITSIssueType.Style);
    setDefaultOff();
  }

  protected String getStartSymbol() {
    return "¿";
  }

  protected String getEndSymbol() {
    return "?";
  }

  @Override
  public int minToCheckParagraph() {
    return 1;
  }

  @Override
  public String getId() {
    return "CA_UNPAIRED_QUESTION";
  }

  @Override
  public String getDescription() {
    return "Exigeix signe d'interrogació inicial";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> matches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      int needsInvQuestionMarkAt = hasTokenAtPos(getEndSymbol(), tokens);
      if (needsInvQuestionMarkAt > 1) {
        boolean hasInvQuestionMark = false;
        // boolean hasInvExlcMark = false;
        AnalyzedTokenReadings firstToken = null;
        for (int i = 0; i < tokens.length; i++) {
          if (firstToken == null && !tokens[i].isSentenceStart()
              && !StringTools.isPunctuationMark(tokens[i].getToken())) {
            firstToken = tokens[i];
          }
          if (tokens[i].getToken().equals(getStartSymbol()) && i < needsInvQuestionMarkAt) {
            hasInvQuestionMark = true;
          }
          // possibly a sentence end
          if (!tokens[i].isSentenceEnd() && tokens[i].getToken().equals(getEndSymbol())
            && i < needsInvQuestionMarkAt) {
            firstToken = null;
          }
          // put the question mark in: ¿de què... ¿de quina
          // put the question mark in: ¿de qué... ¿para cuál... ¿cómo...
          if (i > 2 && i + 2 < tokens.length) {
            if (tokens[i - 1].getToken().equals(",") && tokens[i].hasPosTag("CC") && tokens[i + 1].hasPosTag("SPS00")
              && (tokens[i + 2].hasPosTagStartingWith("PT") || tokens[i + 2].hasPosTagStartingWith("DT"))) {
              firstToken = tokens[i];
            }
            if (tokens[i - 1].getToken().equals(",") && tokens[i].hasPosTag("SPS00")
              && (tokens[i + 1].hasPosTagStartingWith("PT") || tokens[i + 1].hasPosTagStartingWith("DT"))) {
              firstToken = tokens[i];
            }
            if (tokens[i - 1].getToken().equals(",") && tokens[i].hasPosTag("CC")
              && (tokens[i + 1].hasPosTagStartingWith("PT") || tokens[i + 1].hasPosTagStartingWith("DT"))) {
              firstToken = tokens[i];
            }
            if (tokens[i - 1].getToken().equals(",")
              && (tokens[i].hasPosTagStartingWith("PT") || tokens[i].hasPosTagStartingWith("DT"))) {
              firstToken = tokens[i];
            }
            if (tokens[i - 1].getToken().equals(",") && tokens[i].hasPosTag("CC")
              && (tokens[i + 1].getToken().equals("no") || tokens[i + 1].getToken().equals("sí"))) {
              firstToken = tokens[i];
            }
          }
          if (i > 2 && i < tokens.length) {
            if (tokens[i - 1].getToken().equals(",")
              && (tokens[i].getToken().equals("no") || tokens[i].getToken().equals("sí")
              || tokens[i].getToken().equals("oi") || tokens[i].getToken().equals("eh"))) {
              firstToken = tokens[i];
            }
          }
        }
        if (firstToken != null) {
          String s = null;
          if (needsInvQuestionMarkAt > 1 && !hasInvQuestionMark) {
            s = getStartSymbol();
          }
          if (s != null) { // && !prevSentEndsWithColon: skip sentences with ':' due to unclear sentence
                           // boundaries
            String message = "Símbol sense parella: Sembla que falta un '" + s + "'";
            RuleMatch match = new RuleMatch(this, sentence, pos + firstToken.getStartPos(),
                pos + firstToken.getEndPos(), message);
            match.setSuggestedReplacement(s + firstToken.getToken());
            matches.add(match);
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
      // prevSentEndsWithColon = endsWithColon;
    }
    return toRuleMatchArray(matches);
  }

  private int hasTokenAtPos(String ch, AnalyzedTokenReadings[] tokens) {
    int i = tokens.length - 1;
    while (i > 0) {
      if (tokens[i].getToken().equals(ch)) {
        return i;
      }
      i--;
    }
    return -1;
  }
}
