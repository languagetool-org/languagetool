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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.rules.CategoryId;

import java.util.*;

public class InputSentenceTest {
  
  @Test
  public void test() {
    Language lang = Languages.getLanguageForShortCode("xx-XX");
    UserConfig userConfig1 = new UserConfig(Arrays.asList("foo1", "foo2"));
    List<Language> altLanguages = new ArrayList<>();
    JLanguageTool.Mode mode = JLanguageTool.Mode.ALL;
    JLanguageTool.Level level = JLanguageTool.Level.DEFAULT;
    InputSentence inputSentence1a = new InputSentence("foo", lang, lang,
            new HashSet<>(Collections.singletonList("ID1")), new HashSet<>(Collections.singletonList(new CategoryId("C1"))),
            new HashSet<>(Collections.singletonList("ID2")), new HashSet<>(Collections.singletonList(new CategoryId("C2"))), userConfig1, altLanguages, mode, level);
    InputSentence inputSentence1b = new InputSentence("foo", lang, lang,
            new HashSet<>(Collections.singletonList("ID1")), new HashSet<>(Collections.singletonList(new CategoryId("C1"))),
            new HashSet<>(Collections.singletonList("ID2")), new HashSet<>(Collections.singletonList(new CategoryId("C2"))), userConfig1, altLanguages, mode, level);
    Assertions.assertEquals(inputSentence1a, inputSentence1b);
    InputSentence inputSentence2 = new InputSentence("foo", lang, null,
            new HashSet<>(Collections.singletonList("ID1")), new HashSet<>(Collections.singletonList(new CategoryId("C1"))),
            new HashSet<>(Collections.singletonList("ID2")), new HashSet<>(Collections.singletonList(new CategoryId("C2"))), userConfig1, altLanguages, mode, level);
    Assertions.assertNotEquals(inputSentence1a, inputSentence2);
    Assertions.assertNotEquals(inputSentence1b, inputSentence2);
    UserConfig userConfig2 = new UserConfig(Arrays.asList("foo1", "foo2"));
    InputSentence inputSentence1bUserConfig2 = new InputSentence("foo", lang, lang,
            new HashSet<>(Collections.singletonList("ID1")), new HashSet<>(Collections.singletonList(new CategoryId("C1"))),
            new HashSet<>(Collections.singletonList("ID2")), new HashSet<>(Collections.singletonList(new CategoryId("C2"))), userConfig2, altLanguages, mode, level);
    Assertions.assertEquals(inputSentence1a, inputSentence1bUserConfig2);

    InputSentence inputSentence1aOtherMode = new InputSentence("foo", lang, lang,
            new HashSet<>(Collections.singletonList("ID1")), new HashSet<>(Collections.singletonList(new CategoryId("C1"))),
            new HashSet<>(Collections.singletonList("ID2")), new HashSet<>(Collections.singletonList(new CategoryId("C2"))), userConfig1, altLanguages, JLanguageTool.Mode.TEXTLEVEL_ONLY, level);
    Assertions.assertNotEquals(inputSentence1a, inputSentence1aOtherMode);

    List<Language> altLanguages1 = new ArrayList<>();
    List<Language> altLanguages2 = Collections.singletonList(Languages.getLanguageForShortCode("xx"));
    InputSentence inputSentence1aSameAltLang = new InputSentence("foo", lang, lang,
            new HashSet<>(Collections.singletonList("ID1")), new HashSet<>(Collections.singletonList(new CategoryId("C1"))),
            new HashSet<>(Collections.singletonList("ID2")), new HashSet<>(Collections.singletonList(new CategoryId("C2"))), userConfig1, altLanguages1, mode, level);
    Assertions.assertEquals(inputSentence1a, inputSentence1aSameAltLang);
    InputSentence inputSentence1aOtherAltLang = new InputSentence("foo", lang, lang,
            new HashSet<>(Collections.singletonList("ID1")), new HashSet<>(Collections.singletonList(new CategoryId("C1"))),
            new HashSet<>(Collections.singletonList("ID2")), new HashSet<>(Collections.singletonList(new CategoryId("C2"))), userConfig1, altLanguages2, mode, level);
    Assertions.assertNotEquals(inputSentence1a, inputSentence1aOtherAltLang);
  }

}
