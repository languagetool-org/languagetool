/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.junit.jupiter.api.Test;
import org.hamcrest.MatcherAssert;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.core.Is.is;

@SuppressWarnings("MagicNumber")
public class RegexPatternRuleTest {

  @Test
  public void testMatch() throws IOException {
    JLanguageTool lt = new JLanguageTool(TestTools.getDemoLanguage());
    Rule rule = lt.getPatternRulesByIdAndSubId("REGEX_PATTERN_RULE_DEMO_MARK_0", "1").get(0);

    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("This is a test"));
    MatcherAssert.assertThat(matches1.length, is(0));

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("This is foo bar"));
    MatcherAssert.assertThat(matches2.length, is(1));
    MatcherAssert.assertThat(matches2[0].getFromPos(), is(8));
    MatcherAssert.assertThat(matches2[0].getToPos(), is(15));

    RuleMatch[] matches3 = rule.match(lt.getAnalyzedSentence("This is foo bar and fou bar"));
    MatcherAssert.assertThat(matches3.length, is(2));
    
    MatcherAssert.assertThat(matches3[0].getFromPos(), is(8));
    MatcherAssert.assertThat(matches3[0].getToPos(), is(15));
    MatcherAssert.assertThat(matches3[0].getMessage(), is("msg: <suggestion>a suggestion foo</suggestion>"));
    MatcherAssert.assertThat(matches3[0].getSuggestedReplacements().toString(), is("[a suggestion foo, another suggestion bar]"));

    MatcherAssert.assertThat(matches3[1].getFromPos(), is(20));
    MatcherAssert.assertThat(matches3[1].getToPos(), is(27));
    MatcherAssert.assertThat(matches3[1].getMessage(), is("msg: <suggestion>a suggestion fou</suggestion>"));
    MatcherAssert.assertThat(matches3[1].getSuggestedReplacements().toString(), is("[a suggestion fou, another suggestion bar]"));
  }
 
  @Test
  public void testMatchWithMark() throws IOException {
    JLanguageTool lt = new JLanguageTool(TestTools.getDemoLanguage());
    Rule rule = lt.getPatternRulesByIdAndSubId("REGEX_PATTERN_RULE_DEMO_MARK_1", "1").get(0);

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("This is foo bar"));
    MatcherAssert.assertThat(matches2.length, is(1));
    MatcherAssert.assertThat(matches2[0].getFromPos(), is(8));
    MatcherAssert.assertThat(matches2[0].getToPos(), is(11));
 }
}