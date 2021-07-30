/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

public class ProhibitedCompoundRuleTest {

  private final static Map<String, Integer> map = new HashMap<>();
  static {
    map.put("Mietauto",100);
    map.put("Leerzeile",100);
    map.put("Urberliner",100);
    map.put("Ureinwohner",100);
    map.put("Wohnungsleerstand",50);
    map.put("Xliseihflehrstand",50);
    map.put("Eisensande",100);
    map.put("Eisenstange",101);
  }
  private final ProhibitedCompoundRule testRule = new ProhibitedCompoundRule(TestTools.getEnglishMessages(), new FakeLanguageModel(map), null);
  private final JLanguageTool testLt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));

  @Test
  @Ignore("for interactive use, e.g. after extending the list of pairs")
  public void testListOfWords() throws IOException {
    //File input = new File("/home/dnaber/data/corpus/jan_schreiber/german.dic");
    File input = new File("/tmp/words.txt");
    LuceneLanguageModel lm = new LuceneLanguageModel(new File("/home/dnaber/data/google-ngram-index/de/"));
    System.out.println("Words matched by rule:");
    ProhibitedCompoundRule rule = new ProhibitedCompoundRule(TestTools.getEnglishMessages(), lm, null);
    int i = 0;
    try (Scanner sc = new Scanner(input)) {
      while (sc.hasNextLine()) {
        String line = sc.nextLine().trim();
        RuleMatch[] matches = rule.match(testLt.getAnalyzedSentence(line));
        if (matches.length > 0) {
          System.out.println(line + " -> " + matches[0].getSuggestedReplacements());
        }
        i++;
        //if (i % 10_000 == 0) {
        //  System.out.println(i + "...");
        //}
      }
    }
    System.out.println("DONE.");
  }

  @Test
  public void testRule() throws IOException {
    assertMatches("Er ist Uhrberliner.", "Uhrberliner", "Urberliner");
    assertMatches("Er ist Uhr-Berliner.", "Uhr-Berliner", "Urberliner");
    assertMatches("Das ist ein Mitauto.", "Mitauto", "Mietauto");
    assertMatches("Das ist ein Mit-Auto.", "Mit-Auto", "Mietauto");
    assertMatches("Hier leben die Uhreinwohner.", "Uhreinwohner", "Ureinwohner");
    assertMatches("Hier leben die Uhr-Einwohner.", "Uhr-Einwohner", "Ureinwohner");
    assertMatches("Eine Leerzeile einfügen.", 0);
    assertMatches("Eine Leer-Zeile einfügen.", 0);
    assertMatches("Eine Lehrzeile einfügen.", "Lehrzeile", "Leerzeile");
    assertMatches("Eine Lehr-Zeile einfügen.", "Lehr-Zeile", "Leerzeile");

    assertMatches("Viel Wohnungsleerstand.", 0);
    assertMatches("Viel Wohnungs-Leerstand.", 0);
    assertMatches("Viel Wohnungslehrstand.", "Wohnungslehrstand", "Wohnungsleerstand");
    assertMatches("Viel Wohnungs-Lehrstand.", "Wohnungs-Lehrstand", "Wohnungsleerstand");
    assertMatches("Viel Xliseihfleerstand.", 0);
    assertMatches("Viel Xliseihflehrstand.", 0);  // no correct spelling, so not suggested
    assertMatches("Ein kosmografischer Test", 0);
    assertMatches("Ein Elektrokardiograph", 0);
    assertMatches("Die Elektrokardiographen", 0);

    assertMatches("Den Lehrzeile-Test einfügen.", "Lehrzeile", "Leerzeile");
    assertMatches("Die Test-Lehrzeile einfügen.", "Lehrzeile", "Leerzeile");
    assertMatches("Die Versuchs-Test-Lehrzeile einfügen.", "Lehrzeile", "Leerzeile");
    assertMatches("Den Versuchs-Lehrzeile-Test einfügen.", "Lehrzeile", "Leerzeile");
  }

  @Test
  public void testMoreThanOneCandidate() throws IOException {
    assertMatches("Die Eisenstande.", "Eisenstande", "Eisenstange");
    Map<String, Integer> map = new HashMap<>();
    map.put("Eisensande", 101);
    map.put("Eisenstange", 100);
    ProhibitedCompoundRule rule = new ProhibitedCompoundRule(TestTools.getEnglishMessages(), new FakeLanguageModel(map), null);
    RuleMatch[] matches = rule.match(testLt.getAnalyzedSentence("Die Eisenstande"));
    assertThat(matches[0].getSuggestedReplacements().toString(), is("[Eisensande]"));
  }

  @Test
  public void testRemoveHyphensAndAdaptCase() {
    assertNull(testRule.removeHyphensAndAdaptCase("Marathonläuse"));
    assertThat(testRule.removeHyphensAndAdaptCase("Marathon-Läuse"), is("Marathonläuse"));
    assertThat(testRule.removeHyphensAndAdaptCase("Marathon-Läuse-Test"), is("Marathonläusetest"));
    assertThat(testRule.removeHyphensAndAdaptCase("Marathon-läuse-test"), is("Marathonläusetest"));
    assertThat(testRule.removeHyphensAndAdaptCase("viele-Läuse-Test"), is("vieleläusetest"));
    assertNull(testRule.removeHyphensAndAdaptCase("S-Bahn"));
  }

  void assertMatches(String input, int expectedMatches) throws IOException {
    assertMatches(input, expectedMatches, testRule, testLt);
  }

  void assertMatches(String input, int expectedMatches, ProhibitedCompoundRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat("Got matches: " + Arrays.toString(matches), matches.length, is(expectedMatches));
  }

  void assertMatches(String input, String expectedMarkedText, String expectedSuggestions) throws IOException {
    RuleMatch[] matches = testRule.match(testLt.getAnalyzedSentence(input));
    assertThat("Got matches: " + Arrays.toString(matches), matches.length, is(1));
    String markedText = input.substring(matches[0].getFromPos(), matches[0].getToPos());
    assertThat(markedText, is(expectedMarkedText));
    assertThat(matches[0].getSuggestedReplacements().toString(), is("[" + expectedSuggestions + "]"));
  }

  ProhibitedCompoundRule getRule(String languageModelPath) throws IOException {
    return getRule(languageModelPath, ProhibitedCompoundRule.RULE_ID);
  }

  ProhibitedCompoundRule getRule(String languageModelPath, String ruleId) throws IOException {
    Language lang = Languages.getLanguageForShortCode("de");
    LanguageModel languageModel = new LuceneLanguageModel(new File(languageModelPath, lang.getShortCode()));
    List<Rule> rules = lang.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), languageModel, null);
    if (rules == null) {
      throw new RuntimeException("Language " + lang + " doesn't seem to support a language model");
    }
    ProhibitedCompoundRule foundRule = null;
    for (Rule rule : rules) {
      if (rule.getId().equals(ruleId)) {
        foundRule = (ProhibitedCompoundRule) rule;
        break;
      }
    }
    return foundRule;
  }

}
