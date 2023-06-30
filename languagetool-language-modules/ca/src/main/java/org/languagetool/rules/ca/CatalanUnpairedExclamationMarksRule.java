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

public class CatalanUnpairedExclamationMarksRule extends TextLevelRule {

  public CatalanUnpairedExclamationMarksRule(ResourceBundle messages, Language language) {
    setLocQualityIssueType(ITSIssueType.Style);
    setDefaultOff();
  }

  @Override
  public int minToCheckParagraph() {
    return 1;
  }

  @Override
  public String getDescription() {
    return "Exigeix signe d'exclamació inicial";
  }

  @Override
  public String getId() {
    return "CA_UNPAIRED_EXCLAMATION";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> matches = new ArrayList<>();
    //boolean prevSentEndsWithColon = false;
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      // boolean needsInvQuestionMark = hasTokenAtEnd("?", tokens);
      boolean needsInvExclMark = hasTokenAtEnd("!", tokens);
      boolean endsWithColon = hasTokenAtEnd(":", tokens);
      if (needsInvExclMark) {
        // boolean hasInvQuestionMark = false;
        boolean hasInvExlcMark = false;
        AnalyzedTokenReadings firstToken = null;
        for (int i = 0; i < tokens.length; i++) {
          if (firstToken == null && !tokens[i].isSentenceStart()
              && !StringTools.isPunctuationMark(tokens[i].getToken())) {
            firstToken = tokens[i];
          }
          if (tokens[i].getToken().equals("¡")) {
            hasInvExlcMark = true;
          }
          // put the question mark in: ¿de què... ¿de quina
          if (i > 2 && i + 1 < tokens.length) {
            if (tokens[i - 1].getToken().equals(",") && tokens[i].hasPosTag("SPS00")
                && (tokens[i + 1].hasPosTagStartingWith("PT") || tokens[i + 1].hasPosTagStartingWith("DT"))) {
              firstToken = tokens[i];
            }
            if (tokens[i - 1].getToken().equals(",")
                && (tokens[i].hasPosTagStartingWith("PT") || tokens[i].hasPosTagStartingWith("DT"))) {
              firstToken = tokens[i];
            }
          }
        }
        if (firstToken != null) {
          String s = null;
          if (needsInvExclMark && !hasInvExlcMark) {
            s = "¡";
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
      //prevSentEndsWithColon = endsWithColon;
    }
    return toRuleMatchArray(matches);
  }

  private boolean hasTokenAtEnd(String ch, AnalyzedTokenReadings[] tokens) {
    if (tokens[tokens.length - 1].isParagraphEnd() && !tokens[tokens.length - 1].getToken().equals(ch)
        && tokens.length >= 2) {
      return tokens[tokens.length - 2].getToken().equals(ch);
    }
    return tokens[tokens.length - 1].getToken().equals(ch);
  }

}
