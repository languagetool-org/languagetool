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
package org.languagetool.rules.en;

import java.io.IOException;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordCoherencyDataLoader;

/**
 * English version of {@link AbstractWordCoherencyRule}
 */
public class WordCoherencyRule extends AbstractWordCoherencyRule {

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/en/coherency.txt");

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    Map<String, String> shouldNotAppearWord = new HashMap<>(); // e.g. doggie -> doggy
    int pos = 0;

    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (AnalyzedTokenReadings atr : tokens) {
        String surface = atr.getToken();
        String surfaceLc = surface.toLowerCase(Locale.ROOT);

        // Check whether this token is one of the variant candidates
        Set<String> variants = getWordMap().get(surfaceLc);
        if (variants == null || variants.isEmpty()) {
          continue;
        }

        // ====== Key: lemma immunity to prevent false positives (e.g. doggies/doggier/doggiest) ======
        Set<String> lemmasLc = atr.getReadings().stream()
            .map(AnalyzedToken::getLemma)
            .filter(Objects::nonNull)
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(java.util.stream.Collectors.toSet());
        if (!java.util.Collections.disjoint(lemmasLc, variants)) {
           // The lemma itself is one of the coherent variants → inflected form → skip reporting
          continue;
        }
        // ========================================================================

        int fromPos = pos + atr.getStartPos();
        int toPos = pos + atr.getEndPos();

        // If an alternative spelling has already been encountered, this word is the opposite variant → create a match
        if (shouldNotAppearWord.containsKey(surfaceLc)) {
          String other = shouldNotAppearWord.get(surfaceLc);
          String msg = getMessage(surface, other);
          RuleMatch rm = new RuleMatch(this, sentence, fromPos, toPos, msg);

          String marked = sentence.getText().substring(atr.getStartPos(), atr.getEndPos());
          String replacement = createReplacement(marked, surfaceLc, other, atr);
          if (org.languagetool.tools.StringTools.startsWithUppercase(surface)) {
            replacement = org.languagetool.tools.StringTools.uppercaseFirstChar(replacement);
          }
          if (!marked.equalsIgnoreCase(replacement)) {
            rm.setSuggestedReplacement(replacement);
            ruleMatches.add(rm);
          }
          rm.setShortMessage(getShortMessage());
        } else {
          // Record the variant spelling so that later occurrences of the opposite form can be detected
          for (String v : variants) {
            shouldNotAppearWord.put(v, surfaceLc);
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    addExamplePair(Example.wrong("He likes archaeology. Really? She likes <marker>archeology</marker>, too."),
        Example.fixed("He likes archaeology. Really? She likes <marker>archaeology</marker>, too."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "Do not mix variants of the same word ('" + word1 + "' and '" + word2 + "') within a single text.";
  }

  @Override
  public String getId() {
    return "EN_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Coherent spelling of words with two admitted variants.";
  }

}
