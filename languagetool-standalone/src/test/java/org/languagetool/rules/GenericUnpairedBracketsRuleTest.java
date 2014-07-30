/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.List;

public class GenericUnpairedBracketsRuleTest extends TestCase {

  private JLanguageTool langTool;

  public void testStartSymbolCountEqualsEndSymbolCount() throws IOException {
    for (Language language : Language.LANGUAGES) {
      final int startSymbols = language.getUnpairedRuleStartSymbols().length;
      final int endSymbols = language.getUnpairedRuleEndSymbols().length;
      assertEquals("Different number of start and end symbols for " + language, startSymbols, endSymbols);
    }
  }
  
  public void testRule() throws IOException {
    setUpRule(new MyDemo());
    // correct sentences:
    assertMatches("This is »correct«.", 0);
    assertMatches("»Correct«\n»And »here« it ends.«", 0);
    assertMatches("»Correct. This is more than one sentence.«", 0);
    assertMatches("»Correct. This is more than one sentence.«\n»And »here« it ends.«", 0);
    assertMatches("»Correct«\n\n»And here it ends.«\n\nMore text.", 0);
    assertMatches("This »is also »correct««.", 0);
    assertMatches("Good.\n\nThis »is also »correct««.", 0);
    // incorrect sentences:
    assertMatches("This is not correct«", 1);
    assertMatches("This is »not correct", 1);
    assertMatches("This is correct.\n\n»But this is not.", 1);
    assertMatches("This is correct.\n\nBut this is not«", 1);
    assertMatches("»This is correct«\n\nBut this is not«", 1);
    assertMatches("»This is correct«\n\nBut this »is« not«", 1);
    assertMatches("This is not correct. No matter if it's more than one sentence«", 1);
    assertMatches("»This is not correct. No matter if it's more than one sentence", 1);
  }

  private void setUpRule(Language language) throws IOException {
    langTool = new JLanguageTool(language);
    for (Rule rule : langTool.getAllRules()) {
      langTool.disableRule(rule.getId());
    }
    GenericUnpairedBracketsRule rule = new GenericUnpairedBracketsRule(TestTools.getEnglishMessages(), language);
    langTool.addRule(rule);
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
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
