/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnglishWordRepeatBeginningRuleTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));
    
    // ================== correct sentences ===================
    // two successive sentences that start with the same non-adverb word.
    assertEquals(0, lt.check("This is good. This is good, too.").size());
    // three successive sentences that start with the same exception word ("the").
    assertEquals(0, lt.check("The car. The bicycle. The third sentence with 'the'.").size());

    // =================== errors =============================
    // three successive sentences that start with the same non-exception word (personal pronoun "I") 
    List<RuleMatch> matches1 = lt.check("I think so. I have seen that before. I don't like it.");
    assertEquals(1, matches1.size());
    // check suggestions
    assertThat(matches1.get(0).getSuggestedReplacements().get(0), is("Furthermore, I"));
    assertThat(matches1.get(0).getSuggestedReplacements().get(1), is("Likewise, I"));
    assertThat(matches1.get(0).getSuggestedReplacements().get(2), is("Not only that, but I"));
    // three successive sentences that start with the same non-exception word (personal pronoun "He")
    List<RuleMatch> matches2 = lt.check("He thinks so. He has seen that before. He doesn't like it.");
    assertEquals(1, matches2.size());
    // check suggestions
    assertThat(matches2.get(0).getSuggestedReplacements().get(0), is("Furthermore, he"));
    assertThat(matches2.get(0).getSuggestedReplacements().get(1), is("Likewise, he"));
    assertThat(matches2.get(0).getSuggestedReplacements().get(2), is("Not only that, but he"));
    // two successive sentences that start with one of the saved adverbs ("Also")
    List<RuleMatch> matches3 = lt.check("Also, I play football. Also, I play basketball.");
    assertEquals(1, matches3.size());
    // check suggestions (because the adverbs are contained in a Set it is safer to check if the correct suggestions
    // are contained in the real suggestions)
    assertTrue(matches3.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Additionally")));
    assertTrue(matches3.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Besides")));
    assertTrue(matches3.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Furthermore")));
    assertTrue(matches3.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Moreover")));
    assertTrue(matches3.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("In addition")));
    assertTrue(matches3.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("As well as")));
  }

}