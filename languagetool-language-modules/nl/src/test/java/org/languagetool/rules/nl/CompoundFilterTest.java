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
package org.languagetool.rules.nl;

import org.junit.Test;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CompoundFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final RuleFilter filter = new CompoundFilter();

  @Test
  public void testFilter() {
    assertSuggestion("tv", "meubel", "tv-meubel");
    assertSuggestion("test-tv", "meubel", "test-tv-meubel");
    assertSuggestion("onzin", "tv", "onzin-tv");
    assertSuggestion("auto", "onderdeel", "auto-onderdeel");
    assertSuggestion("test", "e-mail", "test-e-mail");
    assertSuggestion("taxi", "jongen", "taxi-jongen");
    assertSuggestion("rij", "instructeur", "rijinstructeur");
    assertSuggestion("test", "e-mail", "test-e-mail");
    assertSuggestion("ANWB", "wagen", "ANWB-wagen");
    assertSuggestion("pro-deo", "advocaat", "pro-deoadvocaat");
  }

  private void assertSuggestion(String word1, String word2, String expectedSuggestion) {
    RuleMatch ruleMatch = filter.acceptRuleMatch(match, makeMap(word1, word2), -1, null);
    assertThat(ruleMatch.getSuggestedReplacements().size(), is(1));
    assertThat(ruleMatch.getSuggestedReplacements().get(0), is(expectedSuggestion));
  }

  private Map<String, String> makeMap(String word1, String word2) {
    Map<String,String> map = new HashMap<>();
    map.put("word1", word1);
    map.put("word2", word2);
    return map;
  }

/*
  public void testFilter() {
    assertSuggestion("tv|meubel", "tv-meubel");
    assertSuggestion("test-tv|meubel", "test-tv-meubel");
    assertSuggestion("onzin|tv", "onzin-tv");
    assertSuggestion("auto|onderdeel", "auto-onderdeel");
    assertSuggestion("test|e-mail", "test-e-mail");
    assertSuggestion("taxi|jongen", "taxi-jongen");
    assertSuggestion("rij|instructeur", "rijinstructeur");
    assertSuggestion("test|e-mail", "test-e-mail");
    assertSuggestion("ANWB|wagen", "ANWB-wagen");
    assertSuggestion("pro-deo|advocaat", "pro-deoadvocaat");
    assertSuggestion("ANWB|tv|wagen", "ANWB-tv-wagen");
  }

  private void assertSuggestion(String words, String expectedSuggestion) {
    RuleMatch ruleMatch = filter.acceptRuleMatch(match, makeMap(words), -1, null);
    assertThat(ruleMatch.getSuggestedReplacements().size(), is(1));
    assertThat(ruleMatch.getSuggestedReplacements().get(0), is(expectedSuggestion));
  }

  private Map<String, String> makeMap(String words) {
    Map<String,String> map = new HashMap<>();
    map.put("words", word1);
    return map;
  }

*/
}
