/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber
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
import org.languagetool.JLanguageTool;
import org.languagetool.UserConfig;
import org.languagetool.language.Demo;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;

public class DictionarySpellMatchFilterTest {

  @Test
  public void testGetPhrases() throws IOException {
    String text = "This is aa bb and then xx yyy zzzz";
    AnnotatedText aText = new AnnotatedTextBuilder().addText(text).build();
    AnalyzedSentence sentence = new JLanguageTool(new Demo()).getAnalyzedSentence(text);
    SpellingFakeRule rule = new SpellingFakeRule();
    List<RuleMatch> matches = Arrays.asList(
      new RuleMatch(rule, sentence, 8, 10, "fake msg"),
      new RuleMatch(rule, sentence, 11, 13, "fake msg"),
      new RuleMatch(rule, sentence, 23, 25, "fake msg"),
      new RuleMatch(rule, sentence, 26, 29, "fake msg"),
      new RuleMatch(rule, sentence, 30, 34, "fake msg")
    );
    DictionarySpellMatchFilter filter = new DictionarySpellMatchFilter(new UserConfig());
    Map<String, List<RuleMatch>> result = filter.getPhrases(matches, aText);
    assertThat(result.size(), is(3));
    assertTrue(result.containsKey("aa bb"));
    assertTrue(result.containsKey("xx yyy"));
    assertTrue(result.containsKey("xx yyy zzzz"));
  }

  static class SpellingFakeRule extends FakeRule {
    @Override
    public boolean isDictionaryBasedSpellingRule() {
      return true;
    }
  }

}
