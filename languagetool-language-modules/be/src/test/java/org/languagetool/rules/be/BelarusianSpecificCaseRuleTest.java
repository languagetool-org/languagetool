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
package org.languagetool.rules.be;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class BelarusianSpecificCaseRuleTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("be"));
  private final BelarusianSpecificCaseRule rule = new BelarusianSpecificCaseRule(TestTools.getEnglishMessages());

  @Test
  public void testRule() throws IOException {
    assertGood("Беларуская Народная Рэспубліка");
    assertGood("Папа Рымскі");
    assertBad("дзяржаўны сцяг Рэспублікі Беларусь");
    assertBad("вярхоўны суд рэспублікі беларусь");

   RuleMatch[] matches1 = assertBad("Мне падабаецца air France.");
    assertThat(matches1[0].getFromPos(), is(15));
    assertThat(matches1[0].getToPos(), is(25));
    assertThat(matches1[0].getSuggestedReplacements().toString(), is("[Air France]"));
    assertThat(matches1[0].getMessage(), is("Уласныя імёны і назвы пішуцца з вялікай літары."));
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
