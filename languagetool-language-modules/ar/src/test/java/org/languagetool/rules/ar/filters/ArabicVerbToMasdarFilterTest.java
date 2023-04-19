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
package org.languagetool.rules.ar.filters;

import org.junit.Test;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.rules.ar.ArabicWordinessRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tagging.ar.ArabicTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArabicVerbToMasdarFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final RuleFilter filter = new ArabicVerbToMafoulMutlaqFilter();
  private final ArabicTagger tagger = new ArabicTagger();

  @Test
  public void testFilter() throws IOException {
    assertSuggestion("يعمل", "يعمل عملًا جيدًا");
  }

  private void assertSuggestion(String word, String expectedSuggestion) throws IOException {
    String word2 = "بأسلوب";
    String word3 = "جيد";
    Map<String, String> args = new HashMap<>();
    args.put("verb", word);
    args.put("adj", word3);
    List<AnalyzedTokenReadings> patternTokens = tagger.tag(asList(word, word2, word3));
    AnalyzedTokenReadings[] patternTokensArray = patternTokens.toArray(new AnalyzedTokenReadings[0]);
    RuleMatch ruleMatch = filter.acceptRuleMatch(match, args, -1, patternTokensArray);
    assertThat(ruleMatch, notNullValue());
    assertThat(ruleMatch.getSuggestedReplacements(), hasItem(expectedSuggestion));
  }

  protected static Map<String, List<String>> loadFromPath(String path) {
    return new SimpleReplaceDataLoader().loadWords(path);
  }

}
