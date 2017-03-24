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
package org.languagetool.tokenizers;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SRXSentenceTokenizerTest {

  @Test
  public void testOfficeFootnoteTokenize() {
    int count = 0;
    for (Language language : Languages.get()) {
      if (language.getSentenceTokenizer().getClass() != SRXSentenceTokenizer.class) {
        continue;
      }
      if (language.getShortCode().equals("km") || language.getShortCode().equals("ml")) {
        // TODO: I don't know about these...
        continue;
      }
      String input = "A sentence.\u0002 And another one.";
      SentenceTokenizer tokenizer = new SRXSentenceTokenizer(language);
      assertEquals("Sentence not split correctly for " + language + ": '" + input + "'",
              "[A sentence.\u0002 , And another one.]", tokenizer.tokenize(input).toString());
      count++;
    }
    if (count == 0) {
      fail("No languages found for testing");
    }
  }

}
