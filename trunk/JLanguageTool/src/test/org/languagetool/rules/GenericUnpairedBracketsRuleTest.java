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
package de.danielnaber.languagetool.rules;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;

public class GenericUnpairedBracketsRuleTest extends TestCase {

  public void testStartSymbolCountEqualsEndSymbolCount() throws IOException {
    for (Language language : Language.LANGUAGES) {
      final int startSymbols = language.getUnpairedRuleStartSymbols().length;
      final int endSymbols = language.getUnpairedRuleEndSymbols().length;
      assertEquals("Different number of start and end symbols for " + language, startSymbols, endSymbols);
    }
  }
  
  public void testRuleGerman() throws IOException {
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.GERMAN);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("(Das sind die Sätze, die die testen sollen)."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule
        .match(langTool.getAnalyzedSentence("Die „Sätze zum testen."));
    assertEquals(1, matches.length);
  }

  public void testRuleSpanish() throws IOException {
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.SPANISH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.SPANISH);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("Soy un hombre (muy honrado)."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("De dónde vas?"));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("¡Atención"));
    assertEquals(1, matches.length);
  }

  public void testRuleFrench() throws IOException {
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.FRENCH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.FRENCH);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("(Qu'est ce que c'est ?)"));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule
        .match(langTool.getAnalyzedSentence("(Qu'est ce que c'est ?"));
    assertEquals(1, matches.length);
  }

  public void testRuleDutch() throws IOException {
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.DUTCH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.DUTCH);
    // correct sentences:
    matches = rule
        .match(langTool
            .getAnalyzedSentence("Het centrale probleem van het werk is de ‘dichterlijke kuischheid’."));
    assertEquals(0, matches.length);
    //this was a bug as there are several pairs that start with the same char:
    matches = rule
    .match(langTool
        .getAnalyzedSentence(" Eurlings: “De gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart”."));
    assertEquals(0, matches.length);
    matches = rule
    .match(langTool
        .getAnalyzedSentence(" Eurlings: \u201eDe gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart\u201d."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule
        .match(langTool
            .getAnalyzedSentence("Het centrale probleem van het werk is de „dichterlijke kuischheid."));
    assertEquals(1, matches.length);
    matches = rule
    .match(langTool
        .getAnalyzedSentence(" Eurlings: “De gegevens van de dienst zijn van cruciaal belang voor de veiligheid van de luchtvaart en de scheepvaart."));
    assertEquals(1, matches.length);
    
  }

  public void testRuleRomanian() throws IOException {
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.ROMANIAN);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.ROMANIAN);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat (pentru puțin timp)."));
    assertEquals(0, matches.length);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("Nu's de prin locurile astea."));
    assertEquals(0, matches.length);
    // cross-bracket matching
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost )plecat( pentru (puțin timp)."));
    assertEquals(2, matches.length); 
    // cross-bracket matching
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost {plecat) pentru (puțin timp}."));
    assertEquals(4, matches.length); 
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat pentru „puțin timp”."));
    assertEquals(0, matches.length);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat „pentru... puțin timp”."));
    assertEquals(0, matches.length);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat „pentru... «puțin» timp”."));
    assertEquals(0, matches.length);
    // correct sentences ( " is _not_ a Romanian symbol - just
    // ignore it, the correct form is [„] (start quote) and [”] (end quote)
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat \"pentru puțin timp."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat „pentru... puțin timp."));
    assertEquals(1, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("A fost plecat «puțin."));
    assertEquals(1, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat „pentru «puțin timp”."));
    assertEquals(3, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat „pentru puțin» timp”."));
    assertEquals(3, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("A fost plecat „pentru... puțin» timp”."));
    assertEquals(3, matches.length);
    // cross-bracket matching
    // incorrect sentences:
    matches = rule
    .match(langTool
    .getAnalyzedSentence("A fost plecat „pentru... «puțin” timp»."));
    assertEquals(4, matches.length);
  }
}
