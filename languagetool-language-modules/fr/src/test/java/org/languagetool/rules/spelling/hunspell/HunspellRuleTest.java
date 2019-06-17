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
import org.languagetool.tagging.disambiguation.Disambiguator;

import static org.junit.Assert.*;

public class HunspellRuleTest {

  @Test
  public void testRuleWithFrench() throws Exception {
    final French french = new French();
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("fr"), french, null);
    final JLanguageTool langTool = new JLanguageTool(french);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Un test simple.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Un test simpple.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Le cœur, la sœur.")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("LanguageTool")).length);

    // Tests with dash and apostrophes.
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Il arrive après-demain.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'Haÿ-les-Roses")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("L'Haÿ les Roses")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Aujourd'hui et jusqu'à demain.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Aujourd’hui et jusqu’à demain.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'Allemagne et l'Italie.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L’Allemagne et l’Italie.")).length);
    assertEquals(2, rule.match(langTool.getAnalyzedSentence("L’allemagne et l’italie.")).length);
  }

  @Test
  public void testImmunizedFrenchWord() throws Exception {
    final French french = new French();
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("fr"), french, null);
    JLanguageTool langTool = new JLanguageTool(french);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("languageTool est génial.")).length);

    final French frenchWithDisambiguator = new French(){
      @Override
      public Disambiguator getDisambiguator() {
        return new TestFrenchDisambiguator();
      }
    };
    langTool = new JLanguageTool(frenchWithDisambiguator);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("languageTool est génial.")).length);
  }
}
