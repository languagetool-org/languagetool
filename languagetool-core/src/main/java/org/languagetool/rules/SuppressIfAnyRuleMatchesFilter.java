/* LanguageTool, a natural language style checker 
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;

public class SuppressIfAnyRuleMatchesFilter extends RuleFilter {

  /*
   * Suppress the match if the new suggestion creates any new match with the rule IDs provided
   */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    List<String> ruleIDs = Arrays.asList(getRequired("ruleIDs", arguments).split(","));
    boolean fastOnly = "true".equals(getOptional("fastOnly", arguments, "false"));
    Language language = ((PatternRule) match.getRule()).getLanguage();
    JLanguageTool lt = language.createDefaultJLanguageTool();
    String sentence = match.getSentence().getText();
    for (String replacement : match.getSuggestedReplacements()) {
      // Fast path: reuse the already analyzed sentence and only replace the matched token.
      // This avoids the expensive re-analysis (tokenization, tagging and disambiguation) of
      // the whole sentence.
      AnalyzedSentence partiallyAnalyzed = replaceTokenInAnalyzedSentence(match, replacement, language);
      if (partiallyAnalyzed != null) {
        if (anyRuleMatches(lt, ruleIDs, partiallyAnalyzed, match)) {
          return null;
        }
        // With fastOnly the fast path is authoritative: if it does not match, the rule fires.
        if (fastOnly) {
          continue;
        }
      }
      // Exact path: analyze the whole sentence with the replacement applied.
      String newSentence = sentence.substring(0, match.getFromPos()) + replacement
          + sentence.substring(match.getToPos());
      AnalyzedSentence analyzedSentence = lt.analyzeText(newSentence).get(0);
      if (anyRuleMatches(lt, ruleIDs, analyzedSentence, match)) {
        return null;
      }
    }
    return match;
  }

  private static boolean anyRuleMatches(JLanguageTool lt, List<String> ruleIDs,
                                        AnalyzedSentence analyzedSentence, RuleMatch match) throws IOException {
    for (Rule r: lt.getAllActiveRules()) {
      if (ruleIDs.contains(r.getId())) {
        RuleMatch[] matches = r.match(analyzedSentence);
        for (RuleMatch m : matches) {
          if ((m.getToPos() >= match.getFromPos() && m.getToPos() <= match.getToPos())
            || (match.getToPos() >= m.getFromPos() && match.getToPos() <= m.getToPos())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /*
   * Build a copy of the already analyzed sentence in which the token covered by the match is
   * replaced by the given replacement, with its readings taken from the tagger (i.e. without
   * re-running the disambiguator). Returns null when the replacement cannot be applied this way
   * (multiple tokens, different length, etc.), so the caller must fall back to a full analysis.
   */
  private static AnalyzedSentence replaceTokenInAnalyzedSentence(RuleMatch match, String replacement,
                                                                 Language language) throws IOException {
    int fromPos = match.getFromPos();
    int toPos = match.getToPos();
    if (replacement.length() != toPos - fromPos || replacement.indexOf(' ') >= 0) {
      return null;
    }
    AnalyzedSentence sentence = match.getSentence();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();
    int idx = -1;
    for (int i = 0; i < tokens.length; i++) {
      if (!tokens[i].isWhitespace() && tokens[i].getStartPos() == fromPos) {
        idx = i;
        break;
      }
    }
    if (idx < 0) {
      return null;
    }
    List<AnalyzedTokenReadings> tagged = language.getTagger().tag(Collections.singletonList(replacement));
    if (tagged.size() != 1) {
      return null;
    }
    AnalyzedTokenReadings[] newTokens = tokens.clone();
    newTokens[idx] = new AnalyzedTokenReadings(tokens[idx], tagged.get(0).getReadings(),
        "SuppressIfAnyRuleMatchesFilter");
    AnalyzedTokenReadings[] newPreDisambigTokens = sentence.getPreDisambigTokens().clone();
    newPreDisambigTokens[idx] = newTokens[idx];
    return new AnalyzedSentence(newTokens, newPreDisambigTokens);
  }

}
