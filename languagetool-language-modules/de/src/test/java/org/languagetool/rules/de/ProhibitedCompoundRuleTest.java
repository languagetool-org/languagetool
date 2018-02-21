/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ProhibitedCompoundRuleTest {
  
  @Test
  public void testRule() throws IOException {
    Map<String,Integer> map = new HashMap<>();
    map.put("Leerzeile", 100);
    map.put("Wohnungsleerstand", 50);
    map.put("Xliseihflehrstand", 50);
    ProhibitedCompoundRule rule = new ProhibitedCompoundRule(TestTools.getEnglishMessages(), new FakeLanguageModel(map));
    JLanguageTool lt = new JLanguageTool(new German());
    assertMatches("Eine Leerzeile einfügen.", 0, rule, lt);
    assertMatches("Eine Lehrzeile einfügen.", 1, rule, lt);
    assertMatches("Viel Wohnungsleerstand.", 0, rule, lt);
    assertMatches("Viel Wohnungslehrstand.", 1, rule, lt);
    assertMatches("Viel Xliseihfleerstand.", 0, rule, lt);
    assertMatches("Viel Xliseihflehrstand.", 0, rule, lt);  // no correct spelling, so not suggested
  }

  private void assertMatches(String input, int expecteMatches, ProhibitedCompoundRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat("Got matches: " + Arrays.toString(matches), matches.length, is(expecteMatches));
  }

}