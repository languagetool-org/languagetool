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
package org.languagetool.rules.nl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.languagetool.rules.nl.SpaceInCompoundRule.generateVariants;

public class SpaceInCompoundRuleTest {

  @Test
  public void testRule() throws IOException {
    SpaceInCompoundRule rule = new SpaceInCompoundRule(TestTools.getEnglishMessages());
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("nl"));
    assertGood("langeafstandloper", rule, lt);
    assertGood("Dat zie je nu weer met de zogenaamde oudelullendagen die in heel andere tijden met gulle hand in cao’s werden uitgereikt aan werknemers van vijftig jaar en ouder.", rule, lt);
    assertGood("...jk aan voor de middelbare school tijdens de aanmeldw...", rule, lt);
    assertMatch("...jk aan voor de middelbare school tijdens de..., lange afstand loper", rule, lt);
    assertMatch("lange afstand loper", rule, lt);
    assertMatch("langeafstand loper", rule, lt);
    assertMatch("lange afstandloper", rule, lt);
    //assertSuggestion("tv meubel", "tv-meubel");
  }

  private void assertGood(String input, SpaceInCompoundRule rule, JLanguageTool lt) throws IOException {
    assertThat(rule.match(lt.getAnalyzedSentence(input)).length, is(0));
  }

  private void assertMatch(String input, SpaceInCompoundRule rule, JLanguageTool lt) throws IOException {
    assertThat(rule.match(lt.getAnalyzedSentence(input)).length, is(1));
  }

  @Test
  public void testVariants() {
    Set<String> res1 = new HashSet<>();
    generateVariants("", Arrays.asList("a", "b"), res1);
    //assertTrue(res1.contains("ab"));
    assertTrue(res1.contains("a b"));
    assertThat(res1.size(), is(1));

    Set<String> res2 = new HashSet<>();
    generateVariants("", Arrays.asList("a", "b", "c"), res2);
    //assertTrue(res2.contains("abc"));
    assertTrue(res2.contains("a b c"));
    assertTrue(res2.contains("ab c"));
    assertTrue(res2.contains("a bc"));
    assertThat(res2.size(), is(3));

    Set<String> res3 = new HashSet<>();
    generateVariants("", Arrays.asList("a", "b", "c", "d"), res3);
    //assertTrue(res3.contains("abcd"));
    assertTrue(res3.contains("a b c d"));
    assertTrue(res3.contains("a bc d"));
    assertTrue(res3.contains("ab cd"));
    assertTrue(res3.contains("ab c d"));
    assertTrue(res3.contains("a b cd"));
    assertTrue(res3.contains("abc d"));
    assertTrue(res3.contains("a bcd"));
    assertThat(res3.size(), is(7));
  }

}
