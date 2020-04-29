/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UpperCaseRuleTest {

  private final Language lang = Languages.getLanguageForShortCode("en");
  private final UpperCaseRule rule = new UpperCaseRule(TestTools.getEnglishMessages(), lang);
  private final JLanguageTool lt = new JLanguageTool(lang);

  @Test
  public void testRule() throws IOException {
    assertGood("The New York Times reviews their gallery all the time.");  // from spelling_global.txt
    assertGood("This Was a Good Idea");  // no dot = no real sentence
    assertGood("Professor Sprout acclimated the plant to a new environment.");  // "Professor ..." = antipattern

    assertMatch("I really Like spaghetti.");
    assertMatch("This Was a good idea.");
    assertMatch("But this Was a good idea.");
    assertMatch("This indeed Was a good idea.");
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertTrue("Expected no matches, got: " + Arrays.toString(matches), matches.length == 0);
  }

  private void assertMatch(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertTrue("Expected 1 match, got: " + Arrays.toString(matches), matches.length == 1);
  }

}
