/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.DiffsAsMatches;
import org.languagetool.tools.PseudoMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.languagetool.rules.ca.CatalanRemoteRewriteHelper.*;
import static org.languagetool.tools.StringTools.trimLeadingAndTrailingSpaces;

/*
  Provide suggestions from an external service
 */
public class CatalanRemoteRewriteFilter extends RuleFilter {

  private static final Logger logger = LoggerFactory.getLogger(CatalanRemoteRewriteFilter.class);
  boolean ab_test = false; //AB test disabled, opened 100%


  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    String originalSentence = trimLeadingAndTrailingSpaces(match.getSentence().getText());
    String correctedSentence = sendPostRequest(originalSentence, match.getRule().getId());
    if (correctedSentence == null || correctedSentence.isEmpty()) {
      return match;
    }
    DiffsAsMatches diffsAsMatches = new DiffsAsMatches();
    List<PseudoMatch> pseudoMatches = diffsAsMatches.getPseudoMatches(originalSentence, correctedSentence);
    PseudoMatch pseudoMatch = diffsAsMatches.getJoinedMatch(pseudoMatches, originalSentence, match.getFromPos() - 2,
      match.getToPos() + 40);
    if (pseudoMatch == null) {
      return match;
    }
    String suggestion = pseudoMatch.getReplacements().get(0);
    String underlined = originalSentence.substring(pseudoMatch.getFromPos(), pseudoMatch.getToPos());
    if ((pseudoMatch.getToPos() == originalSentence.length() || pseudoMatch.getFromPos() == 0) && trimLeadingAndTrailingSpaces(underlined).isEmpty()) {
      return match;
    }
    if (trimLeadingAndTrailingSpaces(suggestion).equals(trimLeadingAndTrailingSpaces(underlined))) {
      return match;
    }
    if (pseudoMatch.getToPos() <= pseudoMatch.getFromPos()) {
      throw new IllegalArgumentException("fromPos (" + pseudoMatch.getFromPos() + ") must be less than toPos ("
        + pseudoMatch.getToPos() + "). Sentence: " + originalSentence);
    }
    RuleMatch newRuleMatch = new RuleMatch(match.getRule(), match.getSentence(), pseudoMatch.getFromPos(),
      pseudoMatch.getToPos(), match.getMessage(), match.getShortMessage());
    newRuleMatch.setSuggestedReplacements(pseudoMatch.getReplacements());
    return newRuleMatch;
  }

}



