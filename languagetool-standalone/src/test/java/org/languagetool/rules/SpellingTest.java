/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class SpellingTest {

  private static final GermanyGerman GERMAN_DE = new GermanyGerman();

  @Test
  public void testEnglishWords() throws Exception {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE, null, null, Collections.singletonList(Languages.getLanguageForShortCode("en-US")), null);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    assertHintMatch("Ein deutscher Text mit dem englischen Wort incomprehensible", rule, lt);
    assertHintMatch("Das Fahrrad heißt auf Englisch bicycle.", rule, lt);
    //assertHintMatch("Er is nach Schweden gefahren.", rule, lt);  // "is" is too short
  }

  private void assertHintMatch(String text, HunspellRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(text));
    /*for (RuleMatch match : matches) {
      System.out.println(match);
      System.out.println(match.getSuggestedReplacements());
    }*/
    assertEquals(1, matches.length);
    assertEquals(RuleMatch.Type.Hint, matches[0].getType());
  }

}
