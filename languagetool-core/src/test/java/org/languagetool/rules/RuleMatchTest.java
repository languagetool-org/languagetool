/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 LanguageTool
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
import org.languagetool.AnalyzedSentence;

import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertSame;

public class RuleMatchTest {

  @Test
  public void testTrimMatchEndsKeepsWhitespaceOnlyMatch() {
    RuleMatch ruleMatch = new RuleMatch(new TestRule(), null, 10, 13, "msg");
    ruleMatch.setOriginalErrorStr(" \u00A0 ");
    ruleMatch.setSuggestedReplacement(" ");

    RuleMatch trimmed = ruleMatch.trimMatchEnds();

    assertSame(ruleMatch, trimmed);
    assertThat(trimmed.getFromPos(), is(10));
    assertThat(trimmed.getToPos(), is(13));
    assertThat(trimmed.getOriginalErrorStr(), is(" \u00A0 "));
    assertThat(trimmed.getSuggestedReplacements().get(0), is(" "));
  }

  @Test
  public void testTrimMatchEndsStillTrimsCommonTokens() {
    RuleMatch ruleMatch = new RuleMatch(new TestRule(), null, 10, 21, "msg");
    ruleMatch.setOriginalErrorStr("foo bar baz");
    ruleMatch.setSuggestedReplacement("foo qux baz");

    RuleMatch trimmed = ruleMatch.trimMatchEnds();

    assertThat(trimmed.getFromPos(), is(14));
    assertThat(trimmed.getToPos(), is(17));
    assertThat(trimmed.getOriginalErrorStr(), is("bar"));
    assertThat(trimmed.getSuggestedReplacements().get(0), is("qux"));
  }

  private static class TestRule extends Rule {

    TestRule() {
      super(ResourceBundle.getBundle("org.languagetool.MessagesBundle"));
    }

    @Override
    public String getId() {
      return "TEST_RULE";
    }

    @Override
    public String getDescription() {
      return "test rule";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) {
      return RuleMatch.EMPTY_ARRAY;
    }
  }
}
