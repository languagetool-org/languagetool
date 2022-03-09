package org.languagetool.rules.el;

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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GreekSpecificCaseRuleTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("el"));
  private final GreekSpecificCaseRule rule = new GreekSpecificCaseRule(TestTools.getEnglishMessages());

  @Test
  public void testRule() throws IOException {
    assertGood("Ηνωμένες Πολιτείες");
    assertGood("Κατοικώ στις Ηνωμένες Πολιτείες.");
    assertGood("Κατοικώ στις ΗΝΩΜΕΝΕΣ ΠΟΛΙΤΕΙΕΣ.");
    assertBad("ηνωμένες πολιτείες");
    assertBad("ηνωμένες Πολιτείες");  
    assertBad("Ηνωμένες πολιτείες");

    RuleMatch[] matches1 = assertBad("Κατοικώ στις Ηνωμένες πολιτείες.");
    assertThat(matches1[0].getFromPos(), is(13));
    assertThat(matches1[0].getToPos(), is(31));
    assertThat(matches1[0].getSuggestedReplacements().toString(), is("[Ηνωμένες Πολιτείες]"));
    assertThat(matches1[0].getMessage(), is("Οι λέξεις της συγκεκριμένης έκφρασης χρείαζεται να ξεκινούν με κεφαλαία γράμματα."));

    RuleMatch[] matches3 = assertBad("Κατοικώ στις Ηνωμένες  πολιτείες."); // note the two spaces
    assertThat(matches3[0].getFromPos(), is(13));
    assertThat(matches3[0].getToPos(), is(32));
  }

  private void assertGood(String input) throws IOException {
    assertThat(rule.match(lt.getAnalyzedSentence(input)).length, is(0));
  }

  private RuleMatch[] assertBad(String input) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(1));
    return matches;
  }
}
