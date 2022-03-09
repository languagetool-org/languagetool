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
package org.languagetool.server;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.language.Demo;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.DictionarySpellMatchFilter;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;

public class DictionarySpellMatchFilterTest {

  @Test
  @Ignore("Used to estimate performance of spell match filter")
  public void testLoading() throws IOException {
    List<String> phrases = JLanguageTool.getDataBroker()
      .getFromResourceDirAsLines("en/multiwords.txt")
      .stream().filter(s -> !s.startsWith("#")).collect(Collectors.toList());
    UserConfig user = new UserConfig(phrases);
    long start = System.currentTimeMillis();
    DictionarySpellMatchFilter filter = new DictionarySpellMatchFilter(user);
    filter.filter(Collections.emptyList(), new AnnotatedTextBuilder().addText("This is a test.").build());
    System.out.printf("Loading filter for %d phrases took %dms.%n", phrases.size(), (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    String text = "This is a test for the phrase filter. " +
      "I'll follow up with a phrase that is part of the multiwords.txt file used to test this." +
      " Rick Astley	NNP Lorem ipsum dolor sit amet.";
    filter.filter(Collections.emptyList(), new AnnotatedTextBuilder().addText(text).build());
    System.out.printf("Applying filter with %d phrases to text with %d chars took %dms.%n",
      phrases.size(), text.length(), (System.currentTimeMillis() - start));
    StringBuilder longText = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longText.append(text);
    }
    filter.filter(Collections.emptyList(), new AnnotatedTextBuilder().addText(longText.toString()).build());
    System.out.printf("Applying filter with %d phrases to text with %d chars took %dms.%n",
      phrases.size(), longText.length(), (System.currentTimeMillis() - start));

    phrases = Arrays.asList("a phrase");
    start = System.currentTimeMillis();
    filter = new DictionarySpellMatchFilter(new UserConfig(phrases));
    filter.filter(Collections.emptyList(), new AnnotatedTextBuilder().addText("This is a test.").build());
    System.out.printf("Loading filter for %d phrases took %dms.%n", phrases.size(), (System.currentTimeMillis() - start));

  }


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

  // Testing languages with spellers based on MorfologikMultiSpeller and HunspellRule
  @Test
  public void testFilterMorfologik() throws Exception {
    Language english = Languages.getLanguageForShortCode("en-US");

    JLanguageTool lt = new JLanguageTool(english);
    assertEquals(1, lt.check("This is a mistak.").size());
    assertEquals(1, lt.check("This is baad.").size());
    assertEquals(2, lt.check("This is a baad mistak.").size());

    UserConfig userConfig = new UserConfig(Arrays.asList("baad mistak"));
    PipelinePool pool = new PipelinePool(new HTTPServerConfig(), null, true);
    JLanguageTool ltWithPhrase = pool.getPipeline(new PipelineSettings(english, userConfig));
    assertEquals(1, ltWithPhrase.check("This is a mistak.").size());
    assertEquals(1, ltWithPhrase.check("This is baad.").size());
    assertEquals(0, ltWithPhrase.check("This is a baad mistak.").size());
  }

  @Test
  public void testPartialMatches() throws Exception {
    // Test what happens when only part of a phrase is misspelled
    // In isolation, it should stay a mistake
    // But in context of the accepted phrase, the misspelled part should be ignored
    Language english = Languages.getLanguageForShortCode("en-US");

    JLanguageTool lt = new JLanguageTool(english);
    assertEquals(0, lt.check("This is a mistake.").size());
    assertEquals(1, lt.check("This is baad.").size());
    assertEquals(1, lt.check("This is a baad mistake.").size());

    UserConfig userConfig = new UserConfig(Arrays.asList("baad mistake"));
    PipelinePool pool = new PipelinePool(new HTTPServerConfig(), null, true);
    JLanguageTool ltWithPhrase = pool.getPipeline(new PipelineSettings(english, userConfig));
    assertEquals(0, ltWithPhrase.check("This is a mistake.").size());
    assertEquals(1, ltWithPhrase.check("This is baad.").size());
    assertEquals(0, ltWithPhrase.check("This is a baad mistake.").size());
  }

  @Test
  public void testFilterHunspell() throws Exception {
    Language german = Languages.getLanguageForShortCode("de-DE");

    JLanguageTool lt = new JLanguageTool(german);
    assertEquals(1, lt.check("Das ist ein Fehlar.").size());
    assertEquals(1, lt.check("Das ist schlim.").size());
    assertEquals(2, lt.check("Das ist ein schlim Fehlar.").size());

    UserConfig userConfig = new UserConfig(Arrays.asList("schlim Fehlar"));
    PipelinePool pool = new PipelinePool(new HTTPServerConfig(), null, true);
    JLanguageTool ltWithPhrase = pool.getPipeline(new PipelineSettings(german, userConfig));
    assertEquals(1, ltWithPhrase.check("Das ist ein Fehlar.").size());
    assertEquals(1, ltWithPhrase.check("Das ist schlim.").size());
    assertEquals(0, ltWithPhrase.check("Das ist ein schlim Fehlar.").size());
  }


  static class SpellingFakeRule extends FakeRule {
    @Override
    public boolean isDictionaryBasedSpellingRule() {
      return true;
    }
  }

}
