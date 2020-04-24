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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CompoundFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final RuleFilter filter = new CompoundFilter();

  @Test
  public void testFilter() {
    assertSuggestion(Arrays.asList("tv", "meubel"), "tv-meubel");
    assertSuggestion(Arrays.asList("test-tv", "meubel"), "test-tv-meubel");
    assertSuggestion(Arrays.asList("onzin", "tv"), "onzin-tv");
    assertSuggestion(Arrays.asList("auto", "onderdeel"), "auto-onderdeel");
    assertSuggestion(Arrays.asList("test", "e-mail"), "test-e-mail");
    assertSuggestion(Arrays.asList("taxi", "jongen"), "taxi-jongen");
    assertSuggestion(Arrays.asList("rij", "instructeur"), "rijinstructeur");
    assertSuggestion(Arrays.asList("test", "e-mail"), "test-e-mail");
    assertSuggestion(Arrays.asList("ANWB", "wagen"), "ANWB-wagen");
    assertSuggestion(Arrays.asList("pro-deo", "advocaat"), "pro-deoadvocaat");
    assertSuggestion(Arrays.asList("ANWB", "tv", "wagen"), "ANWB-tv-wagen");
  }

  private void assertSuggestion(List<String> words, String expectedSuggestion) {
    RuleMatch ruleMatch = filter.acceptRuleMatch(match, makeMap(words), -1, null);
    assertThat(ruleMatch.getSuggestedReplacements().size(), is(1));
    assertThat(ruleMatch.getSuggestedReplacements().get(0), is(expectedSuggestion));
  }

  private Map<String, String> makeMap(List<String> words) {
    Map<String,String> map = new HashMap<>();
    int i = 1;
    for (String word : words) {
      map.put("word" + i, word);
      i++;
    }
    return map;
  }

}
