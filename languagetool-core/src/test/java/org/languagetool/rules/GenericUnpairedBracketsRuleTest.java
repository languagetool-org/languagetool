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
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GenericUnpairedBracketsRuleTest {

  private JLanguageTool langTool;

  @Test
  public void testRule() throws IOException {
    setUpRule(new MyDemo());

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

    assertMatches(1, "This is not correct«");
    assertMatches(1, "This is »not correct");
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

  private void setUpRule(Language language) {
    langTool = new JLanguageTool(language);
    for (Rule rule : langTool.getAllRules()) {
      langTool.disableRule(rule.getId());
    }
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools.getEnglishMessages(), language);
    langTool.addRule(rule);
  }

  private void assertMatches(int expectedMatches, String input) throws IOException {
    List<RuleMatch> ruleMatches = langTool.check(input);
    assertEquals("Expected " + expectedMatches + " matches, got: " + ruleMatches, expectedMatches, ruleMatches.size());
  }

  class MyDemo extends Demo {
    @Override
    public String[] getUnpairedRuleStartSymbols() {
      return new String[]{ "»" };
    }
    @Override
    public String[] getUnpairedRuleEndSymbols() {
      return new String[]{ "«" };
    }
  }
}
