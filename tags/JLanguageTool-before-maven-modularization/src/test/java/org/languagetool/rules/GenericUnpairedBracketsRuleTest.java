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
package org.languagetool.rules;

import java.io.IOException;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

public class GenericUnpairedBracketsRuleTest extends TestCase {

  private GenericUnpairedBracketsRule rule;
  private JLanguageTool langTool;
  
  public void testStartSymbolCountEqualsEndSymbolCount() throws IOException {
    for (Language language : Language.LANGUAGES) {
      final int startSymbols = language.getUnpairedRuleStartSymbols().length;
      final int endSymbols = language.getUnpairedRuleEndSymbols().length;
      assertEquals("Different number of start and end symbols for " + language, startSymbols, endSymbols);
    }
  }
  
  public void testGermanRule() throws IOException {
    setUpRule(Language.GERMAN);
    // correct sentences:
    assertMatches("(Das sind die Sätze, die sie testen sollen).", 0);
    assertMatches("(Das sind die «Sätze», die sie testen sollen).", 0);
    assertMatches("(Das sind die »Sätze«, die sie testen sollen).", 0);
    assertMatches("(Das sind die Sätze (noch mehr Klammern [schon wieder!]), die sie testen sollen).", 0);
    // incorrect sentences:
    assertMatches("Die „Sätze zum Testen.", 1);
    assertMatches("Die «Sätze zum Testen.", 1);
    assertMatches("Die »Sätze zum Testen.", 1);
  }

  public void testSpanishRule() throws IOException {
    setUpRule(Language.SPANISH);
    // correct sentences:
    assertMatches("Soy un hombre (muy honrado).", 0);
    // incorrect sentences:
    assertMatches("De dónde vas?", 1);
    assertMatches("¡Atención", 1);
  }

  public void testFrenchRule() throws IOException {
    setUpRule(Language.FRENCH);
    // correct sentences:
    assertMatches("(Qu'est ce que c'est ?)", 0);
    // incorrect sentences:
    assertMatches("(Qu'est ce que c'est ?", 1);
  }

  public void testDutchRule() throws IOException {
    setUpRule(Language.DUTCH);
    // correct sentences:
    assertMatches("Het centrale probleem van het werk is de ‘dichterlijke kuischheid’.", 0);
    //this was a bug as there are several pairs that start with the same char:
    assertMatches(" Eurlings: “De gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart”.", 0);
    assertMatches(" Eurlings: \u201eDe gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart\u201d.", 0);
    // incorrect sentences:
    assertMatches("Het centrale probleem van het werk is de „dichterlijke kuischheid.", 1);
    assertMatches(" Eurlings: “De gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart.", 1);
  }

  public void testRomanianRule() throws IOException {
    setUpRule(Language.ROMANIAN);
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
    assertMatches("A fost plecat „pentru «puțin timp”.", 3);
    assertMatches("A fost plecat „pentru puțin» timp”.", 3);
    assertMatches("A fost plecat „pentru... puțin» timp”.", 3);
    assertMatches("A fost plecat „pentru... «puțin” timp».", 4);
  }

  private void setUpRule(Language language) throws IOException {
    rule = new GenericUnpairedBracketsRule(TestTools.getEnglishMessages(), language);
    langTool = new JLanguageTool(language);
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(input));
    assertEquals(expectedMatches, matches.length);
  }
}
