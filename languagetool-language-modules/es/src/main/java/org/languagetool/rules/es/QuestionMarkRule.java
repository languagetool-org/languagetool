/* LanguageTool, a natural language style checker 
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.es;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A rule that checks there's a '¿' character if the sentence ends with '?',
 * same for '¡' and '!'.
 *
 * @since 4.8
 */
public class QuestionMarkRule extends TextLevelRule {

  public QuestionMarkRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Typographical);
    addExamplePair(Example.wrong("<marker>Qué</marker> pasa?"), Example.fixed("<marker>¿Qué</marker> pasa?"));
  }

  @Override
  public int minToCheckParagraph() {
    return 1;
  }

  @Override
  public final String getId() {
    return "ES_QUESTION_MARK";
  }

  @Override
  public String getDescription() {
    return "Signos de exclamación / interrogación desparejados";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> matches = new ArrayList<>();
    boolean prevSentEndsWithColon = false;
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      int needsInvQuestionMarkAt = hasTokenAtPos("?", tokens);
      int needsInvExclMarkAt = hasTokenAtPos("!", tokens);
      boolean endsWithColon = tokens[tokens.length -1].getToken().equals(":");
      if (needsInvQuestionMarkAt > 1 || needsInvExclMarkAt > 1) {
        boolean hasInvQuestionMark = false;
        boolean hasInvExlcMark = false;
        AnalyzedTokenReadings firstToken = null;
        for (int i = 0; i < tokens.length; i++) {
          if (firstToken == null && !tokens[i].isSentenceStart()
              && !StringTools.isPunctuationMark(tokens[i].getToken())) {
            firstToken = tokens[i];
          }
          if (tokens[i].getToken().equals("¿") && i < needsInvQuestionMarkAt) {
            hasInvQuestionMark = true;
          } else if (tokens[i].getToken().equals("¡") && i < needsInvExclMarkAt) {
            hasInvExlcMark = true;
          }
          // possibly a sentence end
          if (!tokens[i].isSentenceEnd()
            && (tokens[i].getToken().equals("?") && i > needsInvQuestionMarkAt
            || tokens[i].getToken().equals("!") && i > needsInvExclMarkAt)) {
            firstToken = null;
          }
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
              || tokens[i].getToken().equals("eh"))) {
              firstToken = tokens[i];
            }
          }
        }
        if (firstToken != null && !firstToken.hasPosTag("_english_ignore_")) {
          String s = null;
          if (needsInvQuestionMarkAt > 1 && needsInvExclMarkAt > 1) {
            // ignore for now, e.g. "¡¿Nunca tienes clases o qué?!"
          } else if (needsInvQuestionMarkAt > 1 && !hasInvQuestionMark) {
            s = "¿";
          } else if (needsInvExclMarkAt > 1 && !hasInvExlcMark) {
            s = "¡";
          }
          if (s != null) { // && !prevSentEndsWithColon: skip sentences with ':' due to unclear sentence boundaries
            String message = "Símbolo desparejado: Parece que falta un '" + s + "'";
            RuleMatch match = new RuleMatch(this, sentence, pos + firstToken.getStartPos(),
                pos + firstToken.getEndPos(), message);
            match.setSuggestedReplacement(s + firstToken.getToken());
            matches.add(match);
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
      prevSentEndsWithColon = endsWithColon;
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
