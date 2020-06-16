/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.spelling.hunspell;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.disambiguation.Disambiguator;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HunspellRuleTest {

  private final French french = new French();
  private final HunspellRule rule = new HunspellRule(TestTools.getMessages("fr"), french, null);
  private final JLanguageTool lt = new JLanguageTool(french);

  @Test
  public void testRuleWithFrench() throws Exception {
    test("Un test simple.", 0);
    test("Un test simpple.", 1);
    test("Le cœur, la sœur.", 0);

    test("LanguageTool", 0);
    
    test("L'ONU", 0);

    // Tests with dash and apostrophes.
    test("Il arrive après-demain.", 0);
    test("L'Haÿ-les-Roses", 0);
    test("L'Haÿ les Roses", 1);

    test("Aujourd'hui et jusqu'à demain.", 0);
    test("Aujourd’hui et jusqu’à demain.", 0);
    test("L'Allemagne et l'Italie.", 0);
    test("L’Allemagne et l’Italie.", 0);
    test("L’allemagne et l’italie.", 2);
  }

  private void test(String input, int expectedErrors) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    if (matches.length != expectedErrors) {
      fail("Got != " + expectedErrors + " matches for: " +  input + ": " + Arrays.toString(matches));
    }
  }
  
  @Test
  public void testImmunizedFrenchWord() throws Exception {
    final French french = new French();
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("fr"), french, null);
    JLanguageTool langTool = new JLanguageTool(french);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("languageTool est génial.")).length);

    final French frenchWithDisambiguator = new French(){
      @Override
      public Disambiguator createDefaultDisambiguator() {
        return new TestFrenchDisambiguator();
      }
    };
    langTool = new JLanguageTool(frenchWithDisambiguator);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("languageTool est génial.")).length);
  }
}
