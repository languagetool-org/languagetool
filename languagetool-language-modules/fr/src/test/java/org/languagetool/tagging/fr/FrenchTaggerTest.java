/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.fr;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class FrenchTaggerTest {
  
  private FrenchTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new FrenchTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new French());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("C'est la vie.",
        "C/[C]N m sp|C/[c]N m sp|C/[c]R dem e s -- est/[est]N m s|est/[être]V etre ind pres 3 s -- la/[la]N m sp|la/[la]R pers obj 3 f s|la/[le]D f s -- vie/[vie]N f s", tokenizer, tagger);
    TestTools.myAssert("Je ne parle pas français.",
        "Je/[je]R pers suj 1 s -- ne/[null]null -- parle/[parler]V imp pres 2 s|parle/[parler]V ind pres 1 s|parle/[parler]V ind pres 3 s|parle/[parler]V sub pres 1 s|parle/[parler]V sub pres 3 s -- pas/[pas]N m sp -- français/[français]J m sp|français/[français]N m sp", tokenizer, tagger);
    TestTools.myAssert("blablabla","blablabla/[blablabla]N m s", tokenizer, tagger);
    TestTools.myAssert("passagère","passagère/[passager]J f s|passagère/[passager]N f s", tokenizer, tagger);
    TestTools.myAssert("non-existing-word","non-existing-word/[null]null", tokenizer, tagger);
  }

}
