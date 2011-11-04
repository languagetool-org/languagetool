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
package de.danielnaber.languagetool.tagging.sk;

import java.io.IOException;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class SlovakTaggerTest extends TestCase {
    
  private SlovakTagger tagger;
  private WordTokenizer tokenizer;
      
  public void setUp() {
    tagger = new SlovakTagger();
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
    TestTools.myAssert("Tu nájdete vybrané čísla a obsahy časopisu Kultúra slova.",
        "Tu/[tu]J|Tu/[tu]PD|Tu/[tu]T -- nájdete/[nájsť]VKdpb+ -- vybrané/[vybraný]Gtfp1x|vybrané/[vybraný]Gtfp4x|vybrané/[vybraný]Gtfp5x|vybrané/[vybraný]Gtip1x|vybrané/[vybraný]Gtip4x|vybrané/[vybraný]Gtip5x|vybrané/[vybraný]Gtnp1x|vybrané/[vybraný]Gtnp4x|vybrané/[vybraný]Gtnp5x|vybrané/[vybraný]Gtns1x|vybrané/[vybraný]Gtns4x|vybrané/[vybraný]Gtns5x -- čísla/[číslo]SSnp1|čísla/[číslo]SSnp4|čísla/[číslo]SSnp5|čísla/[číslo]SSns2 -- a/[a]J|a/[a]O|a/[a]Q|a/[a]SUnp1|a/[a]SUnp2|a/[a]SUnp3|a/[a]SUnp4|a/[a]SUnp5|a/[a]SUnp6|a/[a]SUnp7|a/[a]SUns1|a/[a]SUns2|a/[a]SUns3|a/[a]SUns4|a/[a]SUns5|a/[a]SUns6|a/[a]SUns7|a/[a]T|a/[a]W|a/[as]W -- obsahy/[obsah]SSip1|obsahy/[obsah]SSip4|obsahy/[obsah]SSip5 -- časopisu/[časopis]SSis2|časopisu/[časopis]SSis3 -- Kultúra/[kultúra]SSfs1|Kultúra/[kultúra]SSfs5 -- slova/[slovo]SSns2", tokenizer, tagger);        
    TestTools.myAssert("blabla","blabla/[null]null", tokenizer, tagger);        
  }

}
