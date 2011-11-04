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
package de.danielnaber.languagetool.tagging.fr;

import java.io.IOException;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class FrenchTaggerTest extends TestCase {
  
  private FrenchTagger tagger;
  private WordTokenizer tokenizer;

  public void setUp() {
    tagger = new FrenchTagger();
    tokenizer = new WordTokenizer();
  }

  public void testDictionary() throws IOException {
    final Dictionary dictionary = Dictionary.read(
        this.getClass().getResource(tagger.getFileName()));
    final DictionaryLookup dl = new DictionaryLookup(dictionary);
    for (WordData wd : dl) {
      if (wd.getTag() == null || wd.getTag().length() == 0) {
        System.err.println("**** Warning: the word " + wd.getWord() + "/" + wd.getStem() +" lacks a POS tag in the dictionary.");
      }
    }    
  }
  
  public void testTagger() throws IOException {
    TestTools.myAssert("C'est la vie.",
        "C/[C]N m sp|C/[c]N m sp|C/[c]R dem e s -- est/[est]N m s|est/[être]V etre ind pres 3 s -- la/[la]N m sp|la/[la]R pers obj 3 f s|la/[le]D f s -- vie/[vie]N f s", tokenizer, tagger);
    TestTools.myAssert("Je ne parle pas français.",
        "Je/[je]R pers suj 1 s -- ne/[null]null -- parle/[parler]V imp pres 2 s|parle/[parler]V ind pres 1 s|parle/[parler]V ind pres 3 s|parle/[parler]V sub pres 1 s|parle/[parler]V sub pres 3 s -- pas/[pas]N f sp|pas/[pas]N m sp -- français/[français]J m sp|français/[français]N m sp", tokenizer, tagger);
    TestTools.myAssert("blablabla","blablabla/[blablabla]N m s", tokenizer, tagger);
    TestTools.myAssert("passagère","passagère/[passager]J f s|passagère/[passager]N f s", tokenizer, tagger);
    TestTools.myAssert("non_existing_word","non_existing_word/[null]null", tokenizer, tagger);
  }

}
