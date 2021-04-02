/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.FakeLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GenericUnpairedBracketsRuleTest {

  private JLanguageTool lt;

  @Test
  public void testRule() throws IOException {
    setUpRule(new FakeLanguage());

    assertMatches(0, "This is »correct«.");
    assertMatches(0, "»Correct«\n»And »here« it ends.«");
    assertMatches(0, "»Correct. This is more than one sentence.«");
    assertMatches(0, "»Correct. This is more than one sentence.«\n»And »here« it ends.«");
    assertMatches(0, "»Correct«\n\n»And here it ends.«\n\nMore text.");
    assertMatches(0, "»Correct, he said. This is the next sentence.« Here's another sentence.");
    assertMatches(0, "»Correct, he said.\n\nThis is the next sentence.« Here's another sentence.");
    assertMatches(0, "»Correct, he said.\n\n\n\nThis is the next sentence.« Here's another sentence.");
    assertMatches(0, "This »is also »correct««.");
    assertMatches(0, "Good.\n\nThis »is also »correct««.");
    assertMatches(0, "Good.\n\n\nThis »is also »correct««.");
    assertMatches(0, "Good.\n\n\n\nThis »is also »correct««.");
    assertMatches(0, "This is funny :-)");
    assertMatches(0, "This is sad :-( isn't it");
    assertMatches(0, "This is funny :)");
    assertMatches(0, "This is sad :( isn't it");
    assertMatches(0, "a) item one\nb) item two");
    assertMatches(0, "a) item one\nb) item two\nc) item three");
    assertMatches(0, "\na) item one\nb) item two\nc) item three");
    assertMatches(0, "\n\na) item one\nb) item two\nc) item three");
    assertMatches(0, "This is a), not b)");
    assertMatches(0, "This is it (a, not b) some more test");
    assertMatches(0, "This is »not an error yet");
    assertMatches(0, "See https://de.wikipedia.org/wiki/Schlammersdorf_(Adelsgeschlecht)");

    assertMatches(1, "This is not correct«");
    assertMatches(1, "This is »not correct.");
    assertMatches(1, "This is »not an error yet\n\nBut now it has become one");
    assertMatches(1, "This is correct.\n\n»But this is not.");
    assertMatches(1, "This is correct.\n\nBut this is not«");
    assertMatches(1, "»This is correct«\n\nBut this is not«");
    assertMatches(1, "»This is correct«\n\nBut this »is« not«");
    assertMatches(1, "This is not correct. No matter if it's more than one sentence«");
    assertMatches(1, "»This is not correct. No matter if it's more than one sentence");
    assertMatches(1, "Correct, he said. This is the next sentence.« Here's another sentence.");
    assertMatches(1, "»Correct, he said. This is the next sentence. Here's another sentence.");
    assertMatches(1, "»Correct, he said. This is the next sentence.\n\nHere's another sentence.");
    assertMatches(1, "»Correct, he said. This is the next sentence.\n\n\n\nHere's another sentence.");
  }

  @Test
  public void testRuleMatchPositions() throws IOException {
    setUpRule(new FakeLanguage());
    RuleMatch match1 = lt.check("This »is a test.").get(0);
    assertThat(match1.getFromPos(), is(5));
    assertThat(match1.getToPos(), is(6));
    assertThat(match1.getLine(), is(0));
    assertThat(match1.getEndLine(), is(0));
    assertThat(match1.getColumn(), is(5));
    assertThat(match1.getEndColumn(), is(6));

    RuleMatch match2 = lt.check("This.\nSome stuff.\nIt »is a test.").get(0);
    assertThat(match2.getFromPos(), is(21));
    assertThat(match2.getToPos(), is(22));
    assertThat(match2.getLine(), is(2));  // first line is 0
    assertThat(match2.getEndLine(), is(2));
    assertThat(match2.getColumn(), is(4));
    assertThat(match2.getEndColumn(), is(5));

    RuleMatch match3 = lt.check("Th\u00ADis »is a test.").get(0);
    assertThat(match3.getFromPos(), is(6));
    assertThat(match3.getToPos(), is(7));
  }

  private void setUpRule(Language language) {
    lt = new JLanguageTool(language);
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools.getEnglishMessages(),
            Arrays.asList("»"), Arrays.asList("«"));
    lt.addRule(rule);
  }

  private void assertMatches(int expectedMatches, String input) throws IOException {
    List<RuleMatch> ruleMatches = lt.check(input);
    assertEquals("Expected " + expectedMatches + " matches, got: " + ruleMatches, expectedMatches, ruleMatches.size());
  }

  public static GenericUnpairedBracketsRule getBracketsRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllActiveRules()) {
      if (rule instanceof GenericUnpairedBracketsRule) {
        return (GenericUnpairedBracketsRule)rule;
      }
    }
    throw new RuntimeException("Rule not found: " + GenericUnpairedBracketsRule.class);
  }

}
