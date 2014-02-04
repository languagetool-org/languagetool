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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnglishWordRepeatRuleTest {

  private final Language english = Language.getLanguageForShortName("en");
  private final EnglishWordRepeatRule rule = new EnglishWordRepeatRule(TestTools.getEnglishMessages(), english);
  
  private JLanguageTool langTool;

  @Test
  public void testRepeatRule() throws IOException {
    langTool = new JLanguageTool(english);
    assertGood("This is a test.");
    assertGood("If I had had time, I would have gone to see him.");
    assertGood("I don't think that that is a problem.");
    assertBad("You can feel confident that that this administration will continue to support a free and open Internet.");
    assertBad("This is is a test.");
  }

  private void assertGood(String sentence) throws IOException {
    assertMatches(sentence, 0);
  }

  private void assertBad(String sentence) throws IOException {
    assertMatches(sentence, 1);
  }

  private void assertMatches(String sentence, int expectedMatches) throws IOException {
    AnalyzedSentence aSentence = langTool.getAnalyzedSentence(sentence);
    assertThat(rule.match(aSentence).length, is(expectedMatches));
  }

}
