/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.tools.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Check for consistent use of apostrophes (typographic (’) vs. typewriter (')).
 * @since 5.3
 */
public class ConsistentApostrophesRule extends TextLevelRule {

  private final static String contractionRegex = "s|t|ll|d|m|ve|re";

  public ConsistentApostrophesRule(ResourceBundle messages) {
    super(messages);
    setDefaultTempOff(); // TODO
    setUrl(Tools.getUrl("https://languagetool.org/insights/post/punctuation-guide/#what-is-an-apostrophe"));
    addExamplePair(Example.wrong("It's nice, but it <marker>doesn’t</marker> work."),
                   Example.fixed("It's nice, but it <marker>doesn't</marker> work."));
  }

  @Override
  public String getId() {
    return "EN_CONSISTENT_APOS";
  }

  @Override
  public String getDescription() {
    return "Checks if the two types of apostrophes (' and ’) are used consistently in a text.";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> matches = new ArrayList<>();
    int pos = 0;
    boolean hasTwoTypes = hasTwoApostropheTypes(sentences);
    if (!hasTwoTypes) {
      return toRuleMatchArray(matches);
    }
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      for (AnalyzedTokenReadings token : tokens) {
        String message = null;
        String repl = null;
        if (token != null && token.getToken().contains("'") && !token.hasTypographicApostrophe()) {
          message = "You used a typewriter-style apostrophe here, but a typographic apostrophe elsewhere in this text.";
          repl = token.getToken().replace("'", "’");
        } else if (token != null && token.getToken().contains("'") && token.hasTypographicApostrophe()) {
          message = "You used a typographic apostrophe here, but a typewriter-style apostrophe elsewhere in this text.";
          repl = token.getToken();
        }
        if (message != null) {
          RuleMatch match = new RuleMatch(this, sentence, pos + token.getStartPos(), pos + token.getEndPos(),
                              message + " Both are correct, but consider using the same type everywhere in your text.");
          match.setSuggestedReplacement(repl);
          matches.add(match);
        }
        
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(matches);
  }

  private boolean hasTwoApostropheTypes(List<AnalyzedSentence> sentences) {
    boolean hasTypewriterStyle = false;
    boolean hasTypographicStyle = false;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      for (AnalyzedTokenReadings token : tokens) {
        if (token != null && token.getToken().contains("'") && !token.hasTypographicApostrophe()) {
          hasTypewriterStyle = true;
        } else if (token != null && token.getToken().contains("'") && token.hasTypographicApostrophe()) {
          hasTypographicStyle = true;
        }
        if (hasTypewriterStyle && hasTypographicStyle) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int minToCheckParagraph() {
    return -1;
  }

}
