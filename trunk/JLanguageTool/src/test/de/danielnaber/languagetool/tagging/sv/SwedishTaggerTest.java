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
package de.danielnaber.languagetool.tagging.sv;

import java.io.IOException;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class SwedishTaggerTest extends TestCase {
    
  private SwedishTagger tagger;
  private WordTokenizer tokenizer;
      
  public void setUp() {
    tagger = new SwedishTagger();
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
    TestTools.myAssert("Det är nog bäst att du får en klubba till", "Det/[det]PN är/[vara]VB:PRS nog/[nog]AB bäst/[bäst]AB|bäst/[bra]JJ:S|bäst/[god]JJ:S att/[att]KN du/[du]PN får/[får]NN:OF:PLU:NOM:NEU|får/[får]NN:OF:SIN:NOM:NEU|får/[få]VB:PRS en/[en]NN:OF:SIN:NOM:UTR|en/[en]PN|en/[passant]en passant NN:OF:SIN:NOM:UTR|en/[passants]en passant NN:OF:SIN:GEN:UTR|en/[passanten]en passant NN:BF:SIN:NOM:UTR|en/[passantens]en passant NN:BF:SIN:GEN:UTR|en/[passanter]en passant NN:OF:PLU:NOM:UTR|en/[passanters]en passant NN:OF:PLU:GEN:UTR|en/[passanterna]en passant NN:BF:PLU:NOM:UTR|en/[passanternas]en passant NN:BF:PLU:GEN:UTR klubba/[klubba]NN:OF:SIN:NOM:UTR|klubba/[klubba]VB:IMP|klubba/[klubba]VB:INF till/[till]AB|till/[till]PP", tokenizer, tagger);        
    TestTools.myAssert("Du menar sannolikt \"massera\" om du inte skriver om masarnas era förstås.","Du/[du]PN menar/[mena]VB:PRS sannolikt/[sannolikt]AB|sannolikt/[sannolik]JJ:PN massera/[massera]VB:IMP|massera/[massera]VB:INF om/[om]AB|om/[om]KN|om/[om]PP du/[du]PN inte/[inte]AB skriver/[skriva]VB:PRS om/[om]AB|om/[om]KN|om/[om]PP masarnas/[mas]NN:BF:PLU:GEN:UTR era/[era]NN:OF:SIN:NOM:UTR|era/[era]PN förstås/[förstås]AB|förstås/[förstå]VB:INF:PF|förstås/[förstå]VB:PRS:PF", tokenizer, tagger);        

  }

}
