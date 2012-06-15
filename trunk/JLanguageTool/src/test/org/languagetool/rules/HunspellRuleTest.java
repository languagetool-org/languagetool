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

package org.languagetool.rules;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

import org.languagetool.rules.spelling.hunspell.*;

public class HunspellRuleTest {

  @Test
  public void testRule() throws Exception {

    RuleMatch[] matches;
      
    // Catalan
    HunspellRule catRule =
    		new HunspellRule(TestTools.getMessages("Catalan"), Language.CATALAN);
    JLanguageTool catTool = new JLanguageTool(Language.CATALAN);

    // correct sentences:
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence("Allò que més l'interessa.")).length);
    // checks that "WORDCHARS ·-'" is added to Hunspell .aff file
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence("Porta'n quatre al col·legi.")).length);
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence("Has de portar-me'n moltes.")).length);
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence(",")).length);

    //incorrect sentences:
    matches = catRule.match(catTool.getAnalyzedSentence("Pecra"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(5, matches[0].getToPos());
    assertEquals("Pera", matches[0].getSuggestedReplacements().get(0));
  }

  @Test
  public void testRuleWithGerman() throws Exception {
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("German"), Language.GERMANY_GERMAN);
    final JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    commonGermanAsserts(rule, langTool);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);
  }

  @Test
  public void testRuleWithAustrianGerman() throws Exception {
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("German"), Language.AUSTRIAN_GERMAN);
    final JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    commonGermanAsserts(rule, langTool);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);
  }

  @Test
  public void testRuleWithSwissGerman() throws Exception {
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("German"), Language.SWISS_GERMAN);
    final JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    commonGermanAsserts(rule, langTool);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // ß not allowed in Swiss
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);  // ss is used instead of ß
  }

  private void commonGermanAsserts(HunspellRule rule, JLanguageTool langTool) throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestversuch")).length);  // compound
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentest-Versuch")).length);  // compound

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentest-Dftgedgs")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Dftgedgs-Waschmaschinentest")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestdftgedgs")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestversuch orkt")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Arbeitsnehmer")).length);  // wrong infix
    assertEquals(2, rule.match(langTool.getAnalyzedSentence("Der asdegfue orkt")).length);
  }

}
