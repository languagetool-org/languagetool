/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.jupiter.api.Test;
import org.hamcrest.MatcherAssert;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;

public class EnglishSpecificCaseRuleTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
  private final EnglishSpecificCaseRule rule = new EnglishSpecificCaseRule(TestTools.getEnglishMessages());

  @Test
  public void testRule() throws IOException {
    assertGood("Harry Potter");
    assertGood("I like Harry Potter.");
    assertGood("I like HARRY POTTER.");
    assertBad("harry potter");
    assertBad("harry Potter");
    assertBad("Harry potter");

    RuleMatch[] matches1 = assertBad("I like Harry potter.");
    MatcherAssert.assertThat(matches1[0].getFromPos(), is(7));
    MatcherAssert.assertThat(matches1[0].getToPos(), is(19));
    MatcherAssert.assertThat(matches1[0].getSuggestedReplacements().toString(), is("[Harry Potter]"));
    MatcherAssert.assertThat(matches1[0].getMessage(), is("If the term is a proper noun, use initial capitals."));
    
    RuleMatch[] matches2 = assertBad("Alexander The Great");
    MatcherAssert.assertThat(matches2[0].getMessage(), is("If the term is a proper noun, use the suggested capitalization."));

    RuleMatch[] matches3 = assertBad("I like Harry  potter.");  // note the two spaces
    MatcherAssert.assertThat(matches3[0].getFromPos(), is(7));
    MatcherAssert.assertThat(matches3[0].getToPos(), is(20));
  }

  private void assertGood(String input) throws IOException {
    MatcherAssert.assertThat(rule.match(lt.getAnalyzedSentence(input)).length, is(0));
  }

  private RuleMatch[] assertBad(String input) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    MatcherAssert.assertThat(matches.length, is(1));
    return matches;
  }
}
