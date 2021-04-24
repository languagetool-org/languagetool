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
package org.languagetool.rules.es;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SpanishUnpairedBracketsRuleTest {

  private SpanishUnpairedBracketsRule rule;
  private JLanguageTool lt;

  @Test
  public void testSpanishRule() throws IOException {
    lt = new JLanguageTool(new Spanish());
    rule = new SpanishUnpairedBracketsRule(TestTools.getEnglishMessages());
    // correct sentences:
    assertMatches("Soy un hombre (muy honrado).", 0);
    assertMatches("D'Hondt.", 0);
    assertMatches("D’Hondt.", 0);
    assertMatches("L’Équipe", 0);
    assertMatches("rock ’n’ roll", 0);
    assertMatches("Harper's Dictionary of Classical Antiquities", 0);
    assertMatches("Harper’s Dictionary of Classical Antiquities", 0);
    // incorrect sentences:
    assertMatches("Soy un hombre muy honrado).", 1);
    assertMatches("Soy un hombre (muy honrado.", 1);
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
    final RuleMatch[] matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence(input)));
    assertEquals(expectedMatches, matches.length);
  }
}
