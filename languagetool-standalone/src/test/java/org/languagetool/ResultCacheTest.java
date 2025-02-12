/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.junit.Test;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ResultCacheTest {
  
  @Test
  public void testSimpleInputSentenceCache() {
    ResultCache cache = new ResultCache(100);
    assertThat(cache.hitCount(), is(0L));
    assertThat(cache.hitRate(), is(1.0));
    cache.put(new SimpleInputSentence("foo", Languages.getLanguageForShortCode("de")), new AnalyzedSentence(new AnalyzedTokenReadings[]{}));
    assertNotNull(cache.getIfPresent(new SimpleInputSentence("foo", Languages.getLanguageForShortCode("de"))));
    assertNull(cache.getIfPresent(new SimpleInputSentence("foo", Languages.getLanguageForShortCode("de-DE"))));
    assertNull(cache.getIfPresent(new SimpleInputSentence("foo", Languages.getLanguageForShortCode("en"))));
    assertNull(cache.getIfPresent(new SimpleInputSentence("foo bar", Languages.getLanguageForShortCode("de"))));
  }

  @Test
  public void testInputSentenceCache() throws IOException {
    Language langDE = Languages.getLanguageForShortCode("de");
    Language langEN = Languages.getLanguageForShortCode("en");
    JLanguageTool ltDe = new JLanguageTool(langDE);
    JLanguageTool ltEn = new JLanguageTool(langEN);
    ResultCache cache = new ResultCache(100);
    assertThat(cache.hitCount(), is(0L));
    assertThat(cache.hitRate(), is(1.0));
    UserConfig userConfig1 = new UserConfig(List.of("word1"));
    JLanguageTool.Mode mode = JLanguageTool.Mode.ALL;
    JLanguageTool.Level level = JLanguageTool.Level.DEFAULT;
    List<Language> el = Collections.emptyList();
    InputSentence input1a = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig1, el, mode, level);
    InputSentence input1b = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig1, el, mode, level);
    cache.put(input1a, List.of());
    assertNotNull(cache.getIfPresent(input1a));
    assertNotNull(cache.getIfPresent(input1b));
    InputSentence input2a = new InputSentence(ltDe.getAnalyzedSentence("foo bar"), langDE, null, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig1, el, mode, level);
    InputSentence input2b = new InputSentence(ltEn.getAnalyzedSentence("foo"), langEN, null, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig1, el, mode, level);
    InputSentence input2c = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, Languages.getLanguageForShortCode("en"), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig1, el, mode, level);
    InputSentence input2d = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(List.of("ID1")), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig1, el, mode, level);
    assertNull(cache.getIfPresent(input2a));
    assertNull(cache.getIfPresent(input2b));
    assertNull(cache.getIfPresent(input2c));
    assertNull(cache.getIfPresent(input2d));
    
    UserConfig userConfig2 = new UserConfig(List.of("word2"));
    InputSentence input1aUc1 = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig1, el, mode, level);
    assertNotNull(cache.getIfPresent(input1aUc1));
    InputSentence input1aUc2 = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig2, el, mode, level);
    assertNull(cache.getIfPresent(input1aUc2));

    InputSentence input1aUc2Alt = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(),
            new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig2, Collections.singletonList(langEN), mode, level);
    assertNull(cache.getIfPresent(input1aUc2Alt));
    
    //put in cache for next tests
    cache.put(input1aUc2Alt, new ArrayList<>());
    
    Set<ToneTag> toneTagSet1 = new TreeSet<>();
    toneTagSet1.add(ToneTag.positive);
    toneTagSet1.add(ToneTag.clarity);
    Set<ToneTag> toneTagSet2 = new TreeSet<>();
    toneTagSet2.add(ToneTag.general);
    toneTagSet2.add(ToneTag.clarity);
    Set<ToneTag> toneTagSet3 = new TreeSet<>();
    
    InputSentence inputWithTonetagSet1 = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(),
      new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig2, Collections.singletonList(langEN), mode, level, toneTagSet1);

    InputSentence inputWithTonetagSet2 = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(),
      new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig2, Collections.singletonList(langEN), mode, level, toneTagSet2);
    
    InputSentence inputWithTonetagSet3 = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(),
      new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig2, Collections.singletonList(langEN), mode, level, toneTagSet3);

    InputSentence inputWithTonetagSetNull = new InputSentence(ltDe.getAnalyzedSentence("foo"), langDE, null, new HashSet<>(),
      new HashSet<>(), new HashSet<>(), new HashSet<>(), userConfig2, Collections.singletonList(langEN), mode, level, null);
    
    assertNull(cache.getIfPresent(inputWithTonetagSet1));
    cache.put(inputWithTonetagSet1, Collections.singletonList(new RuleMatch(new FakeRule("FAKE1"), 0, 1, "Fake message 1")));
    assertNotNull(cache.getIfPresent(inputWithTonetagSet1));
    
    assertNull(cache.getIfPresent(inputWithTonetagSet2));
    cache.put(inputWithTonetagSet2, Collections.singletonList(new RuleMatch(new FakeRule("FAKE2"), 0, 1, "Fake message 1")));
    assertNotNull(cache.getIfPresent(inputWithTonetagSet2));
    
    assertNotNull(cache.getIfPresent(inputWithTonetagSet3)); // same as input1aUc2Alt
    assertNotNull(cache.getIfPresent(inputWithTonetagSetNull)); // same as input1aUc2Alt
    
    assertNotSame(cache.getIfPresent(inputWithTonetagSet1), cache.getIfPresent(inputWithTonetagSet2));
    assertNotSame(cache.getIfPresent(inputWithTonetagSet1), cache.getIfPresent(input1aUc2Alt));
    assertNotSame(cache.getIfPresent(inputWithTonetagSet2), cache.getIfPresent(input1aUc2Alt));
    assertSame(cache.getIfPresent(inputWithTonetagSet3), cache.getIfPresent(input1aUc2Alt));
    assertSame(cache.getIfPresent(inputWithTonetagSetNull), cache.getIfPresent(input1aUc2Alt));
  }
}
