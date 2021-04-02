/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ro;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Romanian;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class GenericUnpairedBracketsRuleTest {

  private GenericUnpairedBracketsRule rule;
  private JLanguageTool lt;

  @Test
  public void testRomanianRule() throws IOException {
    lt = new JLanguageTool(new Romanian());
    rule = org.languagetool.rules.GenericUnpairedBracketsRuleTest.getBracketsRule(lt);
    // correct sentences:
    assertMatches("A fost plecat (pentru puțin timp).", 0);
    assertMatches("Nu's de prin locurile astea.", 0);
    assertMatches("A fost plecat pentru „puțin timp”.", 0);
    assertMatches("A fost plecat „pentru... puțin timp”.", 0);
    assertMatches("A fost plecat „pentru... «puțin» timp”.", 0);
    // correct sentences ( " is _not_ a Romanian symbol - just
    // ignore it, the correct form is [„] (start quote) and [”] (end quote)
    assertMatches("A fost plecat \"pentru puțin timp.", 0);
    // incorrect sentences:
    assertMatches("A fost )plecat( pentru (puțin timp).", 2);
    assertMatches("A fost {plecat) pentru (puțin timp}.", 4);
    assertMatches("A fost plecat „pentru... puțin timp.", 1);
    assertMatches("A fost plecat «puțin.", 1);
    assertMatches("A fost plecat „pentru «puțin timp”.", 1);
    assertMatches("A fost plecat „pentru puțin» timp”.", 1);
    assertMatches("A fost plecat „pentru... puțin» timp”.", 1);
    assertMatches("A fost plecat „pentru... «puțin” timp».", 4);
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
    final RuleMatch[] matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence(input)));
    assertEquals(expectedMatches, matches.length);
  }
}
