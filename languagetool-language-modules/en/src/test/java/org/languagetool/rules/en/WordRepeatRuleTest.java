/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordRepeatRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WordRepeatRuleTest {

  private final Language english = Languages.getLanguageForShortCode("en");
  private final WordRepeatRule rule = new WordRepeatRule(TestTools.getEnglishMessages(), english);
  private final JLanguageTool lt = new JLanguageTool(english);

  @Test
  public void testRule() throws IOException {
    assertMatches("This is a test sentence.", 0);
    assertMatches("This is a test sentence...", 0);

    // make sure we ignore immunized tokens
    assertMatches("And side to side and top to bottom...", 0);

    assertMatches("This this is a test sentence.", 1);
    assertMatches("This is a test sentence sentence.", 1);
    assertMatches("This is is a a test sentence sentence.", 3);
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertEquals(expectedMatches, matches.length);
  }

}
