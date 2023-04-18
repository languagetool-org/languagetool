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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class ArabicMasdarToVerbFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final RuleFilter filter = new ArabicMasdarToVerbFilter();
  private final ArabicTagger tagger = new ArabicTagger();
  private static final String FILE_NAME = "/ar/arabic_masdar_verb.txt";
  //  boolean debug = true;
  final boolean debug = false;

  @Test
  public void testFilter() throws IOException {
    assertSuggestion("عمل", "يعمل", false);
    assertSuggestion("إعمال", "يعمل", false);
    assertSuggestion("سؤال", "يسأل", false);
    assertSuggestion("أكل", "يأكل", false);
  }

  private void assertSuggestion(String word, String expectedSuggestion, boolean debug) throws IOException {
    String word1 = "يقوم";
    String word2 = "بال" + word;
    Map<String, String> args = new HashMap<>();
    args.put("verb", word1);
    args.put("noun", word2);
    List<AnalyzedTokenReadings> patternTokens = tagger.tag(asList(word1, word2));
    AnalyzedTokenReadings[] patternTokensArray = patternTokens.stream().toArray(AnalyzedTokenReadings[]::new);
    RuleMatch ruleMatch = filter.acceptRuleMatch(match, args, -1, patternTokensArray);

    assertThat(ruleMatch.getSuggestedReplacements().size(), is(1));
    assertThat(ruleMatch.getSuggestedReplacements().get(0), is(expectedSuggestion));
  }


  @Test
  public void testRule() throws IOException {
    Map<String, List<String>> verb2masdarList = loadFromPath(FILE_NAME);
    assertThat(verb2masdarList, notNullValue());
  }


  protected static Map<String, List<String>> loadFromPath(String path) {
    Map<String, List<String>> list = new SimpleReplaceDataLoader().loadWords(path);
    return list;
  }


}
