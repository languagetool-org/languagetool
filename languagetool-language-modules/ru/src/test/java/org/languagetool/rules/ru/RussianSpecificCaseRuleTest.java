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
package org.languagetool.rules.ru;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RussianSpecificCaseRuleTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("ru"));
  private final RussianSpecificCaseRule rule = new RussianSpecificCaseRule(TestTools.getEnglishMessages());

  @Test
  public void testRule() throws IOException {
    assertGood("Рытый Банк");
    assertGood("Центральный банк РФ");
    assertBad("Рытый банк");
    assertBad("центральный банк РФ");

   RuleMatch[] matches1 = assertBad("I like air France.");
    assertThat(matches1[0].getFromPos(), is(7));
    assertThat(matches1[0].getToPos(), is(17));
    assertThat(matches1[0].getSuggestedReplacements().toString(), is("[Air France]"));
    assertThat(matches1[0].getMessage(), is("Для специальных наименований используйте начальную заглавную букву."));
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
