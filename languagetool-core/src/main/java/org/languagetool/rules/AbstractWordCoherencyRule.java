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
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
public abstract class AbstractWordCoherencyRule extends TextLevelRule {

  /**
   * Maps words in both directions, e.g. "aufwendig -&gt; aufwändig" and "aufwändig -&gt; aufwendig".
   * @since 3.0
   */
  protected abstract Map<String, Set<String>> getWordMap();

  /**
   * Get the message shown to the user if the rule matches.
   */
  protected abstract String getMessage(String word1, String word2);
  
  public AbstractWordCoherencyRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
  }
  
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    Map<String, String> shouldNotAppearWord = new HashMap<>();  // e.g. aufwändig -> aufwendig
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (AnalyzedTokenReadings tmpToken : tokens) {
        String token = tmpToken.getToken();
        List<AnalyzedToken> readings = tmpToken.getReadings();
        if (!readings.isEmpty()) {
          Set<String> baseforms = readings.stream().map(AnalyzedToken::getLemma).collect(Collectors.toSet());
          for (String baseform : baseforms) {
            if (baseform != null) {
              token = baseform;
            }
            int fromPos = pos + tmpToken.getStartPos();
            int toPos = pos + tmpToken.getEndPos();
            if (shouldNotAppearWord.containsKey(token)) {
              String otherSpelling = shouldNotAppearWord.get(token);
              String msg = getMessage(token, otherSpelling);
              RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg);
              String marked = sentence.getText().substring(tmpToken.getStartPos(), tmpToken.getEndPos());
              String replacement = marked.replaceFirst("(?i)" + token, otherSpelling);
              if (StringTools.startsWithUppercase(tmpToken.getToken())) {
                replacement = StringTools.uppercaseFirstChar(replacement);
              }
              if (!marked.equalsIgnoreCase(replacement)) {   // see https://github.com/languagetool-org/languagetool/issues/3493
                ruleMatch.setSuggestedReplacement(replacement);
                ruleMatches.add(ruleMatch);
              }
              break;
            } else if (getWordMap().containsKey(token)) {
              Set<String> shouldNotAppearSet = getWordMap().get(token);
              for (String shouldNotAppear : shouldNotAppearSet) {
                shouldNotAppearWord.put(shouldNotAppear, token);
              }
            }
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return -1;
  }

}
