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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.util.Collections.*;
import static org.hamcrest.core.Is.is;

public class MorfologikMultiSpellerTest {

  @Test
  public void testIsMisspelled() throws IOException {
    MorfologikMultiSpeller speller = getSpeller();
    // from test.dict:
    Assertions.assertFalse(speller.isMisspelled("wordone"));
    Assertions.assertFalse(speller.isMisspelled("wordtwo"));
    // from test2.txt:
    Assertions.assertFalse(speller.isMisspelled("Abc"));
    Assertions.assertFalse(speller.isMisspelled("wordthree"));
    Assertions.assertFalse(speller.isMisspelled("wordfour"));
    Assertions.assertFalse(speller.isMisspelled("üblich"));
    Assertions.assertFalse(speller.isMisspelled("schön"));
    Assertions.assertFalse(speller.isMisspelled("Fön"));
    Assertions.assertFalse(speller.isMisspelled("Fün"));
    Assertions.assertFalse(speller.isMisspelled("Fän"));
    // from both test.dict and test2.txt:
    Assertions.assertFalse(speller.isMisspelled("Häuser"));
    // not in any of the files:
    Assertions.assertTrue(speller.isMisspelled("notthere"));
    Assertions.assertTrue(speller.isMisspelled("Fun"));
    Assertions.assertTrue(speller.isMisspelled("Füns"));
    Assertions.assertTrue(speller.isMisspelled("AFün"));
  }

  @Test
  public void testGetSuggestions() throws IOException {
    MorfologikMultiSpeller speller = getSpeller();
    MatcherAssert.assertThat(speller.getSuggestions("wordone").toString(), is("[]"));  // a non-misspelled word
    MatcherAssert.assertThat(speller.getSuggestions("wordones").toString(), is("[wordone]"));
    MatcherAssert.assertThat(speller.getSuggestions("Abd").toString(), is("[Abc]"));
    MatcherAssert.assertThat(speller.getSuggestions("Fxn").toString(), is("[Fän, Fön, Fün]"));
    MatcherAssert.assertThat(speller.getSuggestions("Häusers").toString(), is("[Häuser]"));
  }

  @Test
  public void testInvalidFileName() throws IOException {
    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
      new MorfologikMultiSpeller("/xx/spelling/test.dict.README", singletonList("/xx/spelling/test2.txt"), null, 1);
    });
  }

  @Test
  public void testInvalidFile() throws IOException {
    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
          new MorfologikMultiSpeller("/xx/spelling/no-such-file", singletonList("/xx/spelling/test2.txt"), null, 1);
    });
  }

  private MorfologikMultiSpeller getSpeller() throws IOException {
    return new MorfologikMultiSpeller("/xx/spelling/test.dict", singletonList("/xx/spelling/test2.txt"), null, 1);
  }

}
