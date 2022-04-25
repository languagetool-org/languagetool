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
package org.languagetool.rules.nl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Dutch;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class GenericUnpairedBracketsRuleTest {

  private GenericUnpairedBracketsRule rule;
  private JLanguageTool lt;

  @Test
  public void testDutchRule() throws IOException {
    lt = new JLanguageTool(new Dutch());
    rule = org.languagetool.rules.GenericUnpairedBracketsRuleTest.getBracketsRule(lt);
    // correct sentences:
    assertMatches("Het centrale probleem van het werk is de ‘dichterlijke kuischheid’.", 0);
    //this was a bug as there are several pairs that start with the same char:
    assertMatches(" Eurlings: “De gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart”.", 0);
    assertMatches(" Eurlings: \u201eDe gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart\u201d.", 0);
    // incorrect sentences:
    assertMatches("Het centrale probleem van het werk is de „dichterlijke kuischheid.", 1);
    assertMatches(" Eurlings: “De gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart.", 1);
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
    final RuleMatch[] matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence(input)));
    assertEquals(expectedMatches, matches.length);
  }
}
