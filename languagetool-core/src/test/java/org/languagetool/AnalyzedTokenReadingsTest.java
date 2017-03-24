/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AnalyzedTokenReadingsTest {

  @Test
  public void testNewTags() {
    AnalyzedTokenReadings tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS", "lemma"));
    assertEquals(false, tokenReadings.isLinebreak());
    assertEquals(false, tokenReadings.isSentenceEnd());
    assertEquals(false, tokenReadings.isParagraphEnd());
    assertEquals(false, tokenReadings.isSentenceStart());
    tokenReadings.setSentEnd();
    assertEquals(false, tokenReadings.isSentenceStart());
    assertEquals(true, tokenReadings.isSentenceEnd());
    //test SEND_END or PARA_END added without directly via addReading
    //which is possible e.g. in rule disambiguator 
    tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", null, "lemma"));    
    tokenReadings.addReading(new AnalyzedToken("word", "SENT_END", null));
    assertEquals(true, tokenReadings.isSentenceEnd());
    assertEquals(false, tokenReadings.isParagraphEnd());
    tokenReadings.addReading(new AnalyzedToken("word", "PARA_END", null));
    assertEquals(true, tokenReadings.isParagraphEnd());
    assertEquals(false, tokenReadings.isSentenceStart());
    //but you can't add SENT_START to a non-empty token
    //and get isSentStart == true
    tokenReadings.addReading(new AnalyzedToken("word", "SENT_START", null));
    assertEquals(false, tokenReadings.isSentenceStart());
    AnalyzedToken aTok = new AnalyzedToken("word", "POS", "lemma");
    aTok.setWhitespaceBefore(true);
    tokenReadings = new AnalyzedTokenReadings(aTok);       
    assertEquals(aTok, tokenReadings.getAnalyzedToken(0));
    AnalyzedToken aTok2 = new AnalyzedToken("word", "POS", "lemma");
    assertTrue(!aTok2.equals(tokenReadings.getAnalyzedToken(0)));
    AnalyzedToken aTok3 = new AnalyzedToken("word", "POS", "lemma");
    aTok3.setWhitespaceBefore(true);
    assertEquals(aTok3, tokenReadings.getAnalyzedToken(0));
    AnalyzedTokenReadings testReadings = new AnalyzedTokenReadings(aTok3);
    testReadings.removeReading(aTok3);
    assertTrue(testReadings.getReadingsLength()==1);
    assertEquals("word", testReadings.getToken());
    assertTrue(!testReadings.hasPosTag("POS"));
    //now what about removing something that does not belong to testReadings?
    testReadings.leaveReading(aTok2);
    assertEquals("word", testReadings.getToken());
    assertTrue(!testReadings.hasPosTag("POS"));
    
    testReadings.removeReading(aTok2);
    assertEquals("word", testReadings.getToken());
    assertTrue(!testReadings.hasPosTag("POS"));
  }

  @Test
  public void testToString() {
    AnalyzedTokenReadings tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS", "lemma"));
    assertEquals("word[lemma/POS*]", tokenReadings.toString());
    AnalyzedToken aTok2 = new AnalyzedToken("word", "POS2", "lemma2");
    tokenReadings.addReading(aTok2);
    assertEquals("word[lemma/POS*,lemma2/POS2*]", tokenReadings.toString());
  }

  @Test
  public void testHasPosTag() {
    AnalyzedTokenReadings tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS:FOO:BAR", "lemma"));
    assertTrue(tokenReadings.hasPosTag("POS:FOO:BAR"));
    assertFalse(tokenReadings.hasPosTag("POS:FOO:bar"));
    assertFalse(tokenReadings.hasPosTag("POS:FOO"));
    assertFalse(tokenReadings.hasPosTag("xaz"));
  }

  @Test
  public void testHasPartialPosTag() {
    AnalyzedTokenReadings tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS:FOO:BAR", "lemma"));
    assertTrue(tokenReadings.hasPartialPosTag("POS:FOO:BAR"));
    assertTrue(tokenReadings.hasPartialPosTag("POS:FOO:"));
    assertTrue(tokenReadings.hasPartialPosTag("POS:FOO"));
    assertTrue(tokenReadings.hasPartialPosTag(":FOO:"));
    assertTrue(tokenReadings.hasPartialPosTag("FOO:BAR"));

    assertFalse(tokenReadings.hasPartialPosTag("POS:FOO:BARX"));
    assertFalse(tokenReadings.hasPartialPosTag("POS:foo:BAR"));
    assertFalse(tokenReadings.hasPartialPosTag("xaz"));
  }

  @Test
  public void testMatchesPosTagRegex() {
    AnalyzedTokenReadings tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS:FOO:BAR", "lemma"));
    assertTrue(tokenReadings.matchesPosTagRegex("POS:FOO:BAR"));
    assertTrue(tokenReadings.matchesPosTagRegex("POS:...:BAR"));
    assertTrue(tokenReadings.matchesPosTagRegex("POS:[A-Z]+:BAR"));

    assertFalse(tokenReadings.matchesPosTagRegex("POS:[AB]OO:BAR"));
    assertFalse(tokenReadings.matchesPosTagRegex("POS:FOO:BARX"));
  }

  @Test
  public void testIteration() {
    AnalyzedTokenReadings tokenReadings = new AnalyzedTokenReadings(Arrays.asList(
              new AnalyzedToken("word1", null, null),
              new AnalyzedToken("word2", null, null)), 0);
    int i = 0;
    for (AnalyzedToken tokenReading : tokenReadings) {
      if (i == 0) {
        assertThat(tokenReading.getToken(), is("word1"));
      } else if (i == 1) {
        assertThat(tokenReading.getToken(), is("word2"));
      } else {
        fail();
      }
      i++;
    }
  }

}
