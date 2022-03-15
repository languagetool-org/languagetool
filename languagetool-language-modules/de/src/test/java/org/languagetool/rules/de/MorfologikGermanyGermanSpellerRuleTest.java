/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

@Disabled
public class MorfologikGermanyGermanSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikGermanyGermanSpellerRule rule =
          new MorfologikGermanyGermanSpellerRule(TestTools.getMessages("en"), Languages.getLanguageForShortCode("de-DE"), null, Collections.emptyList());
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Hier stimmt jedes Wort!")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Hir nicht so ganz.")).length);

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Überall äußerst böse Umlaute!")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Üperall äußerst böse Umlaute!")).length);
    
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("daß"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("das", matches[0].getSuggestedReplacements().get(0));  // "dass" would actually be better...
    Assertions.assertEquals("dass", matches[0].getSuggestedReplacements().get(1));
    
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("B(ℓ2)")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("🏽")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("🧡🚴🏽♂️ , 🎉💛✈️")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("компьютерная")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("中文維基百科 中文维基百科")).length);
    
    
  }
  
  @Test
  @Disabled("testing for https://github.com/languagetool-org/languagetool/issues/236")
  public void testFrequency() throws IOException {
    URL fsaURL = JLanguageTool.getDataBroker().getFromResourceDirAsUrl("de/hunspell/de_DE.dict");
    Dictionary dictionary = Dictionary.read(fsaURL);
    Speller speller = new Speller(dictionary, 2);
    MatcherAssert.assertThat(speller.getFrequency("der"), is(25));
    MatcherAssert.assertThat(speller.getFrequency("Haus"), is(11));
    MatcherAssert.assertThat(speller.getFrequency("schön"), is(9));
    MatcherAssert.assertThat(speller.getFrequency("gippsnicht"), is(0));
  }

  @Test
  @Disabled("help testing for https://github.com/morfologik/morfologik-stemming/issues/34")
  public void testCommonMisspellings() throws IOException {
    URL fsaURL = JLanguageTool.getDataBroker().getFromResourceDirAsUrl("de/hunspell/de_DE.dict");
    Dictionary dictionary = Dictionary.read(fsaURL);
    Speller speller = new Speller(dictionary, 2);
    String[] input = (
            // tiny subset from https://de.wikipedia.org/wiki/Wikipedia:Liste_von_Tippfehlern
            "Abenteur Abhängikeit abzuschliessen agerufen Aktivitiäten Aktzeptanz " +
            "Algorhitmus Algoritmus aliiert allgmein Amtsitz änlich Anstoss atakieren begrüsst Bezeichnug chinesiche " +
            "dannach Frima Fahrad Gebaüde gesammt Schrifsteller seperat Septmber Staddteil Rhytmen rhytmisch Maschiene " +
            "Lebensmittelgäschefte enstand großmutter Rytmus " +
            // from user feedback:
            "Vorstelungsgespräch Heißhunge-Attakcen evntl. langwalig Selbstportät Erdgeshoss " +
            "kommmischeweise gegensatz Gesichte Suedkaukasus Englisch-sprachigige " +
            // from gutefrage.net:
            "gerägelt Aufjedenfall ivh hällt daß muß woeder oderso anwalt"
        ).split(" ");
    for (String word : input) {
      check(word, speller);
    }
  }

  private void check(String word, Speller speller) throws CharacterCodingException {
    List<String> suggestions = speller.findReplacements(word);
    /*if (suggestions.size() > 10) {
      suggestions = suggestions.subList(0, 9);
    }*/
    System.out.println(word + ": " + String.join(", ", suggestions));
  }

}
