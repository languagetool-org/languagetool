/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber, Marcin Miłkowski (http://www.languagetool.org)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AnalyzedSentenceTest {

  @Test
  public void testToString() {
    AnalyzedTokenReadings[] words = new AnalyzedTokenReadings[3];
    words[0] = new AnalyzedTokenReadings(new AnalyzedToken("", "SENT_START", null));
    words[1] = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS", "lemma"));
    words[2] = new AnalyzedTokenReadings(new AnalyzedToken(".", "INTERP", null));
    words[2].addReading(new AnalyzedToken(".", "SENT_END", null));
    AnalyzedSentence sentence = new AnalyzedSentence(words);
    assertEquals("<S> word[lemma/POS].[./INTERP,</S>]", sentence.toString());
  }

  @Test
  public void testCopy() {
    AnalyzedTokenReadings[] words = new AnalyzedTokenReadings[3];
    words[0] = new AnalyzedTokenReadings(new AnalyzedToken("", "SENT_START", null));
    words[1] = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS", "lemma"));
    words[2] = new AnalyzedTokenReadings(new AnalyzedToken(".", "INTERP", null));
    words[2].addReading(new AnalyzedToken(".", "SENT_END", null));
    AnalyzedSentence sentence = new AnalyzedSentence(words);
    AnalyzedSentence copySentence = sentence.copy(sentence);
    assertEquals(sentence, copySentence);
    //now change the first sentence
    words[1].immunize(); // this would not work if we stored a copy, which we probably should
    assertEquals("<S> word[lemma/POS{!}].[./INTERP,</S>]", sentence.toString());
    assertNotEquals(sentence, copySentence);
  }

}
