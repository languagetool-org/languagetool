/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://danielnaber.de/)
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
import org.languagetool.TestTools;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LongSentenceRuleTest {

  @Test
  public void testMatch() throws Exception {
    JLanguageTool languageTool = new JLanguageTool(TestTools.getDemoLanguage());
    
    LongSentenceRule rule = new LongSentenceRule(TestTools.getEnglishMessages());
    assertNoMatch(" is a rather short text.", rule, languageTool);
    assertMatch("Now this is not " +
            "a a a a a a a a a a a " +
            "a a a a a a a a a a a " +
            "a a a a a a a a a a a " +
            "a a a a a a a a a a a " +
            "rather that short text.", rule, languageTool);
    
    LongSentenceRule shortRule = new LongSentenceRule(TestTools.getEnglishMessages());
    shortRule.setDefaultValue(6);
    assertNoMatch("This is a rather short text.", shortRule, languageTool);
    assertMatch("This is also a rather short text.", shortRule, languageTool);
    assertNoMatch("These ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ don't count.", shortRule, languageTool);
    assertNoMatch("one two three four five six.", shortRule, languageTool);
    assertNoMatch("one two three (four) five six.", shortRule, languageTool);
    assertMatch("one two three four five six seven.", shortRule, languageTool);
  }

  private void assertNoMatch(String input, LongSentenceRule rule, JLanguageTool languageTool) throws IOException {
    assertThat(rule.match(languageTool.getAnalyzedSentence(input)).length, is(0));
  }

  private void assertMatch(String input, LongSentenceRule rule, JLanguageTool languageTool) throws IOException {
    assertThat(rule.match(languageTool.getAnalyzedSentence(input)).length, is(1));
  }
}
