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
import org.languagetool.rules.CategoryId;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class InputSentenceTest {
  
  @Test
  public void test() {
    Language lang = Languages.getLanguageForShortCode("xx-XX");
    InputSentence inputSentence1a = new InputSentence("foo", lang, lang,
            new HashSet<>(Arrays.asList("ID1")), new HashSet<>(Arrays.asList(new CategoryId("C1"))),
            new HashSet<>(Arrays.asList("ID2")), new HashSet<>(Arrays.asList(new CategoryId("C2"))));
    InputSentence inputSentence1b = new InputSentence("foo", lang, lang,
            new HashSet<>(Arrays.asList("ID1")), new HashSet<>(Arrays.asList(new CategoryId("C1"))),
            new HashSet<>(Arrays.asList("ID2")), new HashSet<>(Arrays.asList(new CategoryId("C2"))));
    assertEquals(inputSentence1a, inputSentence1b);
    InputSentence inputSentence2 = new InputSentence("foo", lang, null,
            new HashSet<>(Arrays.asList("ID1")), new HashSet<>(Arrays.asList(new CategoryId("C1"))),
            new HashSet<>(Arrays.asList("ID2")), new HashSet<>(Arrays.asList(new CategoryId("C2"))));
    assertNotEquals(inputSentence1a, inputSentence2);
    assertNotEquals(inputSentence1b, inputSentence2);
  }

}