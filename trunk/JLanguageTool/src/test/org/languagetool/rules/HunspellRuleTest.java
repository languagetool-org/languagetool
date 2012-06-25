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
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

import org.languagetool.rules.spelling.hunspell.*;

public class HunspellRuleTest {


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

  @Test
  public void testRuleWithFrench() throws Exception {
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("French"), Language.FRENCH);
    final JLanguageTool langTool = new JLanguageTool(Language.FRENCH);

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
  public void testRuleWithBreton() throws Exception {
    final HunspellRule rule = new HunspellRule(TestTools.getMessages("Breton"), Language.BRETON);
    final JLanguageTool langTool = new JLanguageTool(Language.BRETON);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Penaos emañ kont ganit?")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("C'hwerc'h merc'h gwerc'h war c'hwerc'h marc'h kalloc'h")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("C’hwerc’h merc’h gwerc‘h war c‘hwerc‘h marc'h kalloc‘h")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Evel-just")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Evel-juste")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Barrek-tre eo LanguageTool")).length);
  }

  @Ignore("just for internal performance testing, thus ignored by default")
  @Test
  public void testPerformance() throws Exception {
    final List<Language> allLanguages = Language.getAllLanguages();
    for (Language language : allLanguages) {
      final JLanguageTool langTool = new JLanguageTool(language);
      //final HunspellRule rule = new HunspellRule(TestTools.getMessages("German"), language);
      langTool.check("warmup");  // make sure everything is initialized when actually testing
      langTool.check("anotherwarmup");
      final long startTime = System.currentTimeMillis();
      langTool.check("fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf");
      //final AnalyzedSentence analyzedSentence = langTool.getAnalyzedSentence("fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf");
      //rule.match(analyzedSentence);
      final long endTime = System.currentTimeMillis();
      System.out.println((endTime-startTime) + "ms for " + language);
    }
  }

}
