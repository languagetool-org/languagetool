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
package de.danielnaber.languagetool.tagging.pl;

import java.io.IOException;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class PolishTaggerTest extends TestCase {
	
  private PolishTagger tagger;
	private WordTokenizer tokenizer;
	  
	public void setUp() {
	  tagger = new PolishTagger();
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
	  TestTools.myAssert("To jest duży dom.",
	      "To/[ten]adj:sg:acc.nom.voc:n:pos|To/[to]conj -- jest/[być]verb:fin:sg:ter:imperf -- duży/[duży]adj:sg:acc:m3:pos:aff|duży/[duży]adj:sg:nom:m:pos:aff|duży/[duży]adj:sg:voc:m:pos:aff -- dom/[dom]subst:sg:acc.nom:m3", tokenizer, tagger);
    TestTools.myAssert("Krowa pasie się na pastwisku.",
        "Krowa/[krowa]subst:sg:nom:f -- pasie/[pas]subst:sg:loc.voc:m3 -- się/[siebie]qub -- na/[na]prep:acc.loc -- pastwisku/[pastwisko]subst:sg:dat.loc:n", tokenizer, tagger);
    TestTools.myAssert("blablabla", "blablabla/[null]null", tokenizer, tagger);
	}

}
