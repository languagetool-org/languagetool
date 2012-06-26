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

package org.languagetool.tokenizers.cs;

import junit.framework.TestCase;

import org.languagetool.TestTools;
import org.languagetool.tokenizers.SentenceTokenizer;

public class CzechSentenceTokenizerTest extends TestCase {

  private final SentenceTokenizer stokenizer = new CzechSentenceTokenizer();

  public final void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    // From the abbreviation list:
    testSplit("V češtině jsou zkr. i pro jazyky, např. angl., maď. a jiné.");
    testSplit("Titul jako doc. RNDr. Adam Řezník, Ph.D. se může vyskytnout.");
    testSplit("Starověký Egypt vznikl okolo r. 3150 př.n.l. (anebo 3150 př.kr.). ", "A zanikl v r. 31 př.kr.");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
