/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class UpperCaseNgramRuleTest {

  private final Language lang = Languages.getLanguageForShortCode("de");
  private final JLanguageTool lt = new JLanguageTool(lang);

  @Test
  public void testRule() throws IOException {
    Map<String, Integer> map = new HashMap<>();
    map.put("5 Tagen", 100);
    map.put("Sie tagen", 100);
    FakeLanguageModel lm = new FakeLanguageModel(map);
    UpperCaseNgramRule rule = new UpperCaseNgramRule(TestTools.getEnglishMessages(), lm, lang);
    assertMatch(0, "Nach 5 Tagen war es aus.", rule);
    assertMatch(1, "Nach 5 tagen war es aus.", rule);
    assertMatch(0, "Sie tagen im Hotel.", rule);
    assertMatch(1, "Sie Tagen im Hotel.", rule);
  }

  private void assertMatch(int expectedMatches, String input, UpperCaseNgramRule rule) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(expectedMatches));
  }

}
