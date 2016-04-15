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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.io.IOException;
import java.util.*;

/**
 * A rule that matches words for which two different spellings are used
 * throughout the document.
 * 
 * <p>Note that this should not be used for language variations like
 * American English vs. British English or German "alte Rechtschreibung"
 * vs. "neue Rechtschreibung" -- that's the task of a spell checker.
 * 
 * @author Daniel Naber
 * @since 2.7
 */
public abstract class AbstractWordCoherencyRule extends Rule {

  /**
   * Maps words in both directions, e.g. "aufwendig -&gt; aufwändig" and "aufwändig -&gt; aufwendig".
   * @since 3.0
   */
  protected abstract Map<String, String> getWordMap();

  /**
   * Get the message shown to the user if the rule matches.
   */
  protected abstract String getMessage(String word1, String word2);
  
  private final Map<String, RuleMatch> shouldNotAppearWord = new HashMap<>();  // e.g. aufwändig -> RuleMatch of aufwendig

  public AbstractWordCoherencyRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
  }
  
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (AnalyzedTokenReadings tmpToken : tokens) {
      String token = tmpToken.getToken();
      List<AnalyzedToken> readings = tmpToken.getReadings();
      // TODO: in theory we need to care about the other readings, too (affects e.g. German "Schenke" as a noun):
      if (readings.size() > 0) {
        String baseform = readings.get(0).getLemma();
        if (baseform != null) {
          token = baseform;
        }
      }
      if (shouldNotAppearWord.containsKey(token)) {
        RuleMatch otherMatch = shouldNotAppearWord.get(token);
        String otherSpelling = otherMatch.getMessage();
        String msg = getMessage(token, otherSpelling);
        RuleMatch ruleMatch = new RuleMatch(this, tmpToken.getStartPos(), tmpToken.getEndPos(), msg);
        ruleMatch.setSuggestedReplacement(otherSpelling);
        ruleMatches.add(ruleMatch);
      } else if (getWordMap().containsKey(token)) {
        String shouldNotAppear = getWordMap().get(token);
        RuleMatch potentialRuleMatch = new RuleMatch(this, tmpToken.getStartPos(), tmpToken.getEndPos(), token);
        shouldNotAppearWord.put(shouldNotAppear, potentialRuleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
    shouldNotAppearWord.clear();
  }

}
