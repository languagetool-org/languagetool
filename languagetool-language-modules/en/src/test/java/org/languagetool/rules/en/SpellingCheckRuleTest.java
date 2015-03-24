/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SpellingCheckRuleTest {

  @Test
  public void testIgnoreSuggestionsWithMorfologik() throws IOException {
    final JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());

    final List<RuleMatch> matches = langTool.check("This is anArtificialTestWordForLanguageTool.");
    assertEquals(0, matches.size());   // no error, as this word is in ignore.txt

    final List<RuleMatch> matches2 = langTool.check("This is a real typoh.");
    assertEquals(1, matches2.size());
    assertEquals("MORFOLOGIK_RULE_EN_US", matches2.get(0).getRule().getId());

    final List<RuleMatch> matches3 = langTool.check("This is anotherArtificialTestWordForLanguageTol.");  // note the typo
    assertEquals(1, matches3.size());
    assertEquals("[anotherArtificialTestWordForLanguageTool]", matches3.get(0).getSuggestedReplacements().toString());
  }

  @Test
  public void testIsUrl() throws IOException {
    MySpellCheckingRule rule = new MySpellCheckingRule();
    rule.test();
  }

  static class MySpellCheckingRule extends SpellingCheckRule {
    MySpellCheckingRule() {
      super(TestTools.getEnglishMessages(), new AmericanEnglish());
    }
    @Override public String getId() { return null; }
    @Override public String getDescription() { return null; }
    @Override public RuleMatch[] match(AnalyzedSentence sentence) throws IOException { return null; }
    void test() throws IOException {
      assertTrue(isUrl("http://www.test.de"));
      assertTrue(isUrl("http://www.test-dash.com"));
      assertTrue(isUrl("https://www.test-dash.com"));
      assertTrue(isUrl("ftp://www.test-dash.com"));
      assertTrue(isUrl("http://www.test-dash.com/foo/path-dash"));
      assertTrue(isUrl("http://www.test-dash.com/foo/öäü-dash"));
      assertTrue(isUrl("http://www.test-dash.com/foo/%C3%B-dash"));
      assertFalse(isUrl("www.languagetool.org"));  // currently not detected
    }
  }
}
