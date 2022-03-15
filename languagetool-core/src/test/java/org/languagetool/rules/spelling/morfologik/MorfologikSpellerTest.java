/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling.morfologik;

import org.junit.jupiter.api.Test;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

import static org.hamcrest.core.Is.is;

public class MorfologikSpellerTest {

  @Test
  public void testIsMisspelled() throws IOException {
    MorfologikSpeller speller = new MorfologikSpeller("/xx/spelling/test.dict");
    Assertions.assertTrue(speller.convertsCase());

    Assertions.assertFalse(speller.isMisspelled("wordone"));
    Assertions.assertFalse(speller.isMisspelled("Wordone"));
    Assertions.assertFalse(speller.isMisspelled("wordtwo"));
    Assertions.assertFalse(speller.isMisspelled("Wordtwo"));
    Assertions.assertFalse(speller.isMisspelled("Uppercase"));
    Assertions.assertFalse(speller.isMisspelled("Häuser"));

    Assertions.assertTrue(speller.isMisspelled("Hauser"));
    Assertions.assertTrue(speller.isMisspelled("wordones"));
    Assertions.assertTrue(speller.isMisspelled("nosuchword"));
  }

  @Test
  public void testGetSuggestions() {
    MorfologikSpeller spellerDist1 = new MorfologikSpeller("/xx/spelling/test.dict", 1);
    MorfologikSpeller spellerDist2 = new MorfologikSpeller("/xx/spelling/test.dict", 2);

    MatcherAssert.assertThat(spellerDist1.getSuggestions("wordone").toString(), is("[]"));
    MatcherAssert.assertThat(spellerDist1.getSuggestions("wordonex").toString(), is("[wordone/51]"));
    MatcherAssert.assertThat(spellerDist2.getSuggestions("wordone").toString(), is("[]"));
    MatcherAssert.assertThat(spellerDist2.getSuggestions("wordonex").toString(), is("[wordone/51]"));

    MatcherAssert.assertThat(spellerDist1.getSuggestions("wordonix").toString(), is("[]"));
    MatcherAssert.assertThat(spellerDist2.getSuggestions("wordonix").toString(), is("[wordone/77]"));

    MatcherAssert.assertThat(spellerDist2.getSuggestions("wordoxix").toString(), is("[]"));
  }
}
