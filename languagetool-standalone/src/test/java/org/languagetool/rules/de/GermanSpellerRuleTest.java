/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class GermanSpellerRuleTest {

  private static final German GERMAN_DE = (German) Languages.getLanguageForShortCode("de-DE");

  @Test
  public void testErrorLimitReached() throws IOException {
    HunspellRule rule1 = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    RuleMatch[] matches1 = rule1.match(lt.getAnalyzedSentence("Ein schöner Satz."));
    assertThat(matches1.length, is(0));
    RuleMatch[] matches2 = rule1.match(lt.getAnalyzedSentence("But this is English."));
    assertThat(matches2.length, is(4));
    assertNull(matches2[0].getErrorLimitLang());
    assertNull(matches2[1].getErrorLimitLang());
    assertThat(matches2[2].getErrorLimitLang(), is("en"));
    RuleMatch[] matches3 = rule1.match(lt.getAnalyzedSentence("Und er sagte, this is a good test."));
    assertThat(matches3.length, is(4));
    assertNull(matches3[3].getErrorLimitLang());
  }

  @Test
  // case: signature is (mostly) English, user starts typing in German -> first, EN is detected for whole text
  // Also see MorfologikAmericanSpellerRuleTest
  public void testMultilingualSignatureCase() throws IOException {
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    String sig = "-- " +
            "Das ist eine deutsche Signatur.\n" +
            "Eigentlich egal, was hier genau steht. Aber es reicht, um den Gesamttext als deutsch zu erkennen.\n";
    //assertZZ(lt, rule, "Hi Tom, I'm happy to discuss the\n\n" + sig);  // "Hi Tom, I'm happy" also accepted by German speller
    //assertZZ(lt, rule, "Tom, I'm happy to discuss the\n\n" + sig);
    assertZZ(lt, rule, "Tom, could we meet next Monday\n\n" + sig);
  }

  private void assertZZ(JLanguageTool lt, HunspellRule rule, String input) throws IOException {
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(input);
    RuleMatch[] matches = rule.match(analyzedSentences.get(0));
    /*System.out.println("--> " + input);
    for (RuleMatch ruleMatch : matches) {
      System.out.println(">>>" + ruleMatch.getRule().getId() + " " + ruleMatch.getErrorLimitLang());
    }*/
    assertThat(analyzedSentences.size(), is(4));
    assertThat(matches.length, is(5));
    assertNull(matches[0].getErrorLimitLang());
    assertNull(matches[1].getErrorLimitLang());
    assertNull(matches[2].getErrorLimitLang());
    assertThat(matches[3].getErrorLimitLang(), is("en"));
    assertThat(matches[4].getErrorLimitLang(), is("en"));
  }

}
