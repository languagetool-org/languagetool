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

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class MorfologikMultiSpellerTest {

  @Test
  public void testIsMisspelled() throws IOException {
    MorfologikMultiSpeller speller = getSpeller();
    // from test.dict:
    assertFalse(speller.isMisspelled("wordone"));
    assertFalse(speller.isMisspelled("wordtwo"));
    // from test2.txt:
    assertFalse(speller.isMisspelled("Abc"));
    assertFalse(speller.isMisspelled("wordthree"));
    assertFalse(speller.isMisspelled("wordfour"));
    assertFalse(speller.isMisspelled("üblich"));
    assertFalse(speller.isMisspelled("schön"));
    assertFalse(speller.isMisspelled("Fön"));
    assertFalse(speller.isMisspelled("Fün"));
    assertFalse(speller.isMisspelled("Fän"));
    // from both test.dict and test2.txt:
    assertFalse(speller.isMisspelled("Häuser"));
    // not in any of the files:
    assertTrue(speller.isMisspelled("notthere"));
    assertTrue(speller.isMisspelled("Fun"));
    assertTrue(speller.isMisspelled("Füns"));
    assertTrue(speller.isMisspelled("AFün"));
  }

  @Test
  public void testGetSuggestions() throws IOException {
    MorfologikMultiSpeller speller = getSpeller();
    assertThat(speller.getSuggestions("wordone").toString(), is("[]"));  // a non-misspelled word
    assertThat(speller.getSuggestions("wordones").toString(), is("[wordone]"));
    assertThat(speller.getSuggestions("Abd").toString(), is("[Abc]"));
    assertThat(speller.getSuggestions("Fxn").toString(), is("[Fän, Fön, Fün]"));
    assertThat(speller.getSuggestions("Häusers").toString(), is("[Häuser]"));
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidFileName() throws IOException {
    new MorfologikMultiSpeller("/xx/spelling/test.dict.README", "/xx/spelling/test2.txt", null, 1);
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidFile() throws IOException {
    new MorfologikMultiSpeller("/xx/spelling/no-such-file", "/xx/spelling/test2.txt", null, 1);
  }

  private MorfologikMultiSpeller getSpeller() throws IOException {
    return new MorfologikMultiSpeller("/xx/spelling/test.dict", "/xx/spelling/test2.txt", null, 1);
  }

}