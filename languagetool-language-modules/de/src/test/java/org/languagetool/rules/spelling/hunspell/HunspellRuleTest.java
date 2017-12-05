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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.AustrianGerman;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.SwissGerman;
import org.languagetool.rules.de.GermanSpellerRule;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class HunspellRuleTest {

  @Test
  public void testRuleWithGerman() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), new GermanyGerman());
    JLanguageTool langTool = new JLanguageTool(new German());
    commonGermanAsserts(rule, langTool);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);
    // ignore URLs:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Unter http://foo.org/bar steht was.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("dasdassda http://foo.org/bar steht was.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Unter http://foo.org/bar steht dasdassda.")).length);
    
    // check the correct calculation of error position
    // note that emojis have string length 2
    assertEquals(6 ,rule.match(langTool.getAnalyzedSentence("Hallo men Schatz!"))[0].getFromPos());
    assertEquals(9 ,rule.match(langTool.getAnalyzedSentence("Hallo men Schatz!"))[0].getToPos());
    assertEquals(9 ,rule.match(langTool.getAnalyzedSentence("Hallo 😂 men Schatz!"))[0].getFromPos());
    assertEquals(12 ,rule.match(langTool.getAnalyzedSentence("Hallo 😂 men Schatz!"))[0].getToPos());
    assertEquals(11 ,rule.match(langTool.getAnalyzedSentence("Hallo 😂😂 men Schatz!"))[0].getFromPos());
    assertEquals(14 ,rule.match(langTool.getAnalyzedSentence("Hallo 😂😂 men Schatz!"))[0].getToPos());
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Mir geht es 😂gut😂.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Mir geht es 😂gtu😂.")).length);
  }

  @Test
  public void testRuleWithAustrianGerman() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), new AustrianGerman());
    JLanguageTool langTool = new JLanguageTool(new German());
    commonGermanAsserts(rule, langTool);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);
  }

  @Test
  public void testRuleWithSwissGerman() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), new SwissGerman());
    JLanguageTool langTool = new JLanguageTool(new German());
    commonGermanAsserts(rule, langTool);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // ß not allowed in Swiss
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);  // ss is used instead of ß
  }

  private void commonGermanAsserts(HunspellRule rule, JLanguageTool langTool) throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestversuch")).length);  // compound
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentest-Versuch")).length);  // compound
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Arbeitnehmer")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Verhaltensänderung")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentest-Dftgedgs")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Dftgedgs-Waschmaschinentest")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestdftgedgs")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestversuch orkt")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Arbeitsnehmer")).length);  // wrong interfix
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Verhaltenänderung")).length);  // missing interfix
    assertEquals(2, rule.match(langTool.getAnalyzedSentence("Der asdegfue orkt")).length);
  }

  @Ignore("just for internal performance testing, thus ignored by default")
  @Test
  public void testPerformance() throws Exception {
    List<Language> allLanguages = Languages.get();
    for (Language language : allLanguages) {
      JLanguageTool langTool = new JLanguageTool(language);
      //HunspellRule rule = new HunspellRule(TestTools.getMessages("German"), language);
      langTool.check("warmup");  // make sure everything is initialized when actually testing
      langTool.check("anotherwarmup");
      long startTime = System.currentTimeMillis();
      langTool.check("fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf");
      //String[] w = {"foo", "warmup", "Rechtschreipreform", "Theatrekasse", "Zoobesuck", "Handselvertreter", "Mückenstick", "gewönlich", "Traprennen", "Autoverkehrr"};
      //AnalyzedSentence analyzedSentence = langTool.getAnalyzedSentence("fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf");
      //rule.match(analyzedSentence);
      long endTime = System.currentTimeMillis();
      System.out.println((endTime-startTime) + "ms for " + language);
    }
  }

  @Ignore("just for internal performance testing, thus ignored by default")
  @Test
  public void testCompoundAwareRulePerformance() throws IOException {
    ResourceBundle messages = ResourceBundle.getBundle("org.languagetool.MessagesBundle", new Locale("de"));
    //slow:
    //HunspellRule rule = new HunspellRule(messages, Language.GERMANY_GERMAN);
    //fast:
    CompoundAwareHunspellRule rule = new GermanSpellerRule(messages, new GermanyGerman());
    rule.init();
    String[] words = {"foo", "warmup", "Rechtschreipreform", "Theatrekasse", "Zoobesuck", "Handselvertreter", "Mückenstick", "gewönlich", "Traprennen", "Autoverkehrr"};
    for (String word : words) {
      long startTime = System.currentTimeMillis();
      List<String> suggest = rule.getSuggestions(word);
      System.out.println((System.currentTimeMillis()-startTime) + "ms for " + word + ": " + suggest);
    }
  }
  
}
