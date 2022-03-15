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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.de.GermanSpellerRule;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;

public class HunspellRuleTest {

  @Test
  public void testHighConfidenceSuggestion() {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-DE"), null);
    Assertions.assertTrue(rule.isFirstItemHighConfidenceSuggestion("HAus", Collections.singletonList(new SuggestedReplacement("HAus"))));
    Assertions.assertFalse(rule.isFirstItemHighConfidenceSuggestion("EI", Collections.singletonList(new SuggestedReplacement("Eis"))));
    Assertions.assertFalse(rule.isFirstItemHighConfidenceSuggestion("CMs", Collections.singletonList(new SuggestedReplacement("CMS"))));
    Assertions.assertFalse(rule.isFirstItemHighConfidenceSuggestion("DMs", Collections.singletonList(new SuggestedReplacement("DMS"))));
  }
  
  @Test
  public void testRuleWithGerman() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-DE"), null);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    commonGermanAsserts(rule, lt);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der äussere Übeltäter.")).length);
    // ignore URLs:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Unter http://foo.org/bar steht was.")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("dasdassda http://foo.org/bar steht was.")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Unter http://foo.org/bar steht dasdassda.")).length);
    
    // check the correct calculation of error position
    // note that emojis have string length 2 or 3
    Assertions.assertEquals(6, rule.match(lt.getAnalyzedSentence("Hallo men Schatz!"))[0].getFromPos());
    Assertions.assertEquals(9, rule.match(lt.getAnalyzedSentence("Hallo men Schatz!"))[0].getToPos());
    Assertions.assertEquals(9, rule.match(lt.getAnalyzedSentence("Hallo 😂 men Schatz!"))[0].getFromPos());
    Assertions.assertEquals(12, rule.match(lt.getAnalyzedSentence("Hallo 😂 men Schatz!"))[0].getToPos());
    Assertions.assertEquals(11, rule.match(lt.getAnalyzedSentence("Hallo 😂😂 men Schatz!"))[0].getFromPos());
    Assertions.assertEquals(14, rule.match(lt.getAnalyzedSentence("Hallo 😂😂 men Schatz!"))[0].getToPos());
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Mir geht es 😂gut😂.")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Mir geht es 😂gtu😂.")).length);
    Assertions.assertEquals(10, rule.match(lt.getAnalyzedSentence("Hallo 🗺️ men Schatz!"))[0].getFromPos());
    Assertions.assertEquals(13, rule.match(lt.getAnalyzedSentence("Hallo 🗺️ men Schatz!"))[0].getToPos());
    
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("B(ℓ2)")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("🏽")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("🧡🚴🏽♂️ , 🎉💛✈️")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("компьютерная")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("中文維基百科 中文维基百科")).length);
    
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Blu-ray-Brenner")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Stand-by-Betrieb")).length);
    
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("- Teex"));
    Assertions.assertEquals(1, matches.length); 
    Assertions.assertEquals("Tee", matches[0].getSuggestedReplacements().get(0).toString());
    Assertions.assertEquals(2, matches[0].getFromPos());
    Assertions.assertEquals(6, matches[0].getToPos());
    
    matches = rule.match(lt.getAnalyzedSentence("-Teex"));
    Assertions.assertEquals(1, matches.length);
    //assertEquals("[-tee, -telex, -tees, -teen, -teer, -tee-, -text]", matches[0].getSuggestedReplacements().toString()); // Preferably "Tee" !?
    Assertions.assertEquals("[Tee, Telex, Tees, Teen, Teer, Tee-, Texte, TeX, Text]", matches[0].getSuggestedReplacements().toString()); // Preferably "Tee" !?
    Assertions.assertEquals(1, matches[0].getFromPos());
    Assertions.assertEquals(5, matches[0].getToPos());
    
    matches = rule.match(lt.getAnalyzedSentence("- Kaffeex"));
    Assertions.assertEquals(1, matches.length); 
    Assertions.assertEquals("[Kaffee, Kaffees, Kaffee-]", matches[0].getSuggestedReplacements().toString());
    Assertions.assertEquals(2, matches[0].getFromPos());
    Assertions.assertEquals(9, matches[0].getToPos());
    
    matches = rule.match(lt.getAnalyzedSentence("-Kaffeex"));
    Assertions.assertEquals(1, matches.length); 
    //assertEquals("[-kaffee, -kaffees, -kaffee-, -karaffe, Kaffee]", matches[0].getSuggestedReplacements().toString());
    Assertions.assertEquals("[Kaffee, Kaffees, Kaffee-]", matches[0].getSuggestedReplacements().toString());
    Assertions.assertEquals(1, matches[0].getFromPos());
    Assertions.assertEquals(8, matches[0].getToPos());
    
    matches = rule.match(lt.getAnalyzedSentence("E -Commerce"));
    Assertions.assertEquals(2, matches.length); 
    Assertions.assertEquals("[E-Commerce]", matches[0].getSuggestedReplacements().toString());
    Assertions.assertEquals(0, matches[0].getFromPos());
    Assertions.assertEquals(11, matches[0].getToPos());
    //assertEquals("[E-Commerce, C-centromer]", matches[1].getSuggestedReplacements().toString());
    Assertions.assertEquals("[E-Commerce, Comer]", matches[1].getSuggestedReplacements().toString());
    Assertions.assertEquals(3, matches[1].getFromPos());
    Assertions.assertEquals(11, matches[1].getToPos());
    
  }

  @Test
  public void testRuleWithWrongSplit() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-DE"), null);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    assertResult("Vielen Dan kfür die Blumen", "Dank für", 7, 15, rule, lt);
    assertResult("Vielen Dankf ür die Blumen", "Dank für", 7, 15, rule, lt);
    assertResult("Viele nDank für die Blumen", "Vielen Dank", 0, 11, rule, lt);
    assertResult("VielenD ank für die Blumen", "Vielen Dank", 0, 11, rule, lt);
    assertResult("Vielen Dank für di eBlumen", "die Blumen", 16, 26, rule, lt);
    assertResult("Vielen Dank für di eBlumen.", "die Blumen", 16, 26, rule, lt);
    assertResult("Vielen Dank für dieB lumen", "die Blumen", 16, 26, rule, lt);
    assertResult("Vielen Dank für dieB lumen.", "die Blumen", 16, 26, rule, lt);
    assertResult("Das ist g anz falsch", "ganz", 8, 13, rule, lt);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Vielen Dak für dieB lumen"));
    MatcherAssert.assertThat(matches.length, is(3));
    MatcherAssert.assertThat(matches[0].getSuggestedReplacements().get(0), is("DAK"));  // not really a good first suggestion...
    MatcherAssert.assertThat(matches[1].getSuggestedReplacements().size(), is(1));
    MatcherAssert.assertThat(matches[1].getSuggestedReplacements().get(0), is("die Blumen"));
    MatcherAssert.assertThat(matches[1].getFromPos(), is(15));
    MatcherAssert.assertThat(matches[1].getToPos(), is(25));
    MatcherAssert.assertThat(matches[2].getSuggestedReplacements().get(0), is("Lumen"));

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("Und gan viele nDank"));
    MatcherAssert.assertThat(matches2.length, is(3));
    MatcherAssert.assertThat(matches2[0].getSuggestedReplacements().get(0), is("gab"));  // not really a good first suggestion...
    MatcherAssert.assertThat(matches2[1].getSuggestedReplacements().size(), is(1));
    MatcherAssert.assertThat(matches2[1].getSuggestedReplacements().get(0), is("vielen Dank"));
    MatcherAssert.assertThat(matches2[1].getFromPos(), is(8));
    MatcherAssert.assertThat(matches2[1].getToPos(), is(19));
    MatcherAssert.assertThat(matches2[2].getSuggestedReplacements().get(0), is("Dank"));

    RuleMatch[] matches3 = rule.match(lt.getAnalyzedSentence("Vielen Dank für dieB lumen und auuch falsch"));
    MatcherAssert.assertThat(matches3.length, is(3));
    MatcherAssert.assertThat(matches3[0].getSuggestedReplacements().get(0), is("die Blumen"));
    MatcherAssert.assertThat(matches3[1].getSuggestedReplacements().get(0), is("Lumen"));
    MatcherAssert.assertThat(matches3[2].getSuggestedReplacements().get(0), is("auch"));
  }
  
  private void assertResult(String input, String expectedResult, int fromPos, int toPos, HunspellRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    MatcherAssert.assertThat("Got != 2 match(es): " + Arrays.toString(matches), matches.length, is(2));
    MatcherAssert.assertThat(matches[0].getSuggestedReplacements().size(), is(1));
    MatcherAssert.assertThat(matches[0].getSuggestedReplacements().get(0), is(expectedResult));
    MatcherAssert.assertThat(matches[0].getFromPos(), is(fromPos));
    MatcherAssert.assertThat(matches[0].getToPos(), is(toPos));
  }

  @Test
  public void testRuleWithAustrianGerman() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-AT"), null);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    commonGermanAsserts(rule, lt);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der äussere Übeltäter.")).length);
  }

  @Test
  public void testRuleWithSwissGerman() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-CH"), null);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    commonGermanAsserts(rule, lt);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // ß not allowed in Swiss
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Der äussere Übeltäter.")).length);  // ss is used instead of ß
  }

  private void commonGermanAsserts(HunspellRule rule, JLanguageTool lt) throws IOException {
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestversuch")).length);  // compound
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentest-Versuch")).length);  // compound
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Der Arbeitnehmer")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Die Verhaltensänderung")).length);

    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentest-Dftgedgs")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Dftgedgs-Waschmaschinentest")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestdftgedgs")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Waschmaschinentestversuch orkt")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Der Arbeitsnehmer")).length);  // wrong interfix
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Die Verhaltenänderung")).length);  // missing interfix
    Assertions.assertEquals(2, rule.match(lt.getAnalyzedSentence("Der asdegfue orkt")).length);
  }

  @Disabled("just for internal performance testing, thus ignored by default")
  @Test
  public void testPerformance() throws Exception {
    List<Language> allLanguages = Languages.get();
    for (Language language : allLanguages) {
      JLanguageTool lt = new JLanguageTool(language);
      //HunspellRule rule = new HunspellRule(TestTools.getMessages("German"), language);
      lt.check("warmup");  // make sure everything is initialized when actually testing
      lt.check("anotherwarmup");
      long startTime = System.currentTimeMillis();
      lt.check("fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf");
      //String[] w = {"foo", "warmup", "Rechtschreipreform", "Theatrekasse", "Zoobesuck", "Handselvertreter", "Mückenstick", "gewönlich", "Traprennen", "Autoverkehrr"};
      //AnalyzedSentence analyzedSentence = langTool.getAnalyzedSentence("fdfds fdfdsa fdfdsb fdfdsc fdfdsd fdfdse fdfdsf");
      //rule.match(analyzedSentence);
      long endTime = System.currentTimeMillis();
      System.out.println((endTime-startTime) + "ms for " + language);
    }
  }

  @Disabled("just for internal performance testing, thus ignored by default")
  @Test
  public void testCompoundAwareRulePerformance() throws IOException {
    ResourceBundle messages = ResourceBundle.getBundle("org.languagetool.MessagesBundle", new Locale("de"));
    //slow:
    //HunspellRule rule = new HunspellRule(messages, Language.GERMANY_GERMAN);
    //fast:
    CompoundAwareHunspellRule rule = new GermanSpellerRule(messages, (German) Languages.getLanguageForShortCode("de-DE"));
    rule.ensureInitialized();
    String[] words = {"foo", "warmup", "Rechtschreipreform", "Theatrekasse", "Zoobesuck", "Handselvertreter", "Mückenstick", "gewönlich", "Traprennen", "Autoverkehrr"};
    for (String word : words) {
      long startTime = System.currentTimeMillis();
      List<String> suggest = rule.getSuggestions(word);
      System.out.println((System.currentTimeMillis()-startTime) + "ms for " + word + ": " + suggest);
    }
  }
  
}
