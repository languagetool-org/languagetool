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
package de.danielnaber.languagetool.tagging.gl;

import java.io.IOException;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

/**
 * @author Susana Sotelo Docio
 * based on English test
 */
public class GalicianTaggerTest extends TestCase {

  private GalicianTagger tagger;
  private WordTokenizer tokenizer;
  
  public void setUp() {
    tagger = new GalicianTagger();
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
    TestTools.myAssert("Todo vai mudar",
        "Todo/[todo]DI0MS0|Todo/[todo]PI0MS000 -- vai/[ir]VMIP3S0|vai/[ir]VMM02S0 -- mudar/[mudar]VMN0000|mudar/[mudar]VMN01S0|mudar/[mudar]VMN03S0|mudar/[mudar]VMSF1S0|mudar/[mudar]VMSF3S0", tokenizer, tagger);
  }
}
