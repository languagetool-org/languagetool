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
import org.languagetool.language.Demo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LongSentenceRuleTest {

  @Test
  public void testMatch() throws Exception {
    LongSentenceRule rule = new LongSentenceRule(TestTools.getEnglishMessages());
    JLanguageTool languageTool = new JLanguageTool(new Demo());
    assertThat(rule.match(languageTool.getAnalyzedSentence("This is a rather short text.")).length, is(0));
    assertThat(rule.match(languageTool.getAnalyzedSentence("Now this is not " +
            "a a a a a a a a a a a " +
            "a a a a a a a a a a a " +
            "a a a a a a a a a a a " +
            "rather that short text.")).length, is(1));
    LongSentenceRule shortRule = new LongSentenceRule(TestTools.getEnglishMessages(), 6);
    assertThat(shortRule.match(languageTool.getAnalyzedSentence("This is a rather short text.")).length, is(0));
    assertThat(shortRule.match(languageTool.getAnalyzedSentence("This is also a rather short text.")).length, is(1));
    assertThat(shortRule.match(languageTool.getAnalyzedSentence("These ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ don't count.")).length, is(0));
  }
}
