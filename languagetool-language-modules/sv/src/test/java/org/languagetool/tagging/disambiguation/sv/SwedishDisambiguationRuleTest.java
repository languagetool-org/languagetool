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
package org.languagetool.tagging.disambiguation.sv;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Swedish;
import org.languagetool.tagging.sv.SwedishTagger;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

public class SwedishDisambiguationRuleTest {
      
  private SwedishTagger tagger;
  private WordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private MultiWordChunker disambiguator;
    
  @Before
  public void setUp() {
    tagger = new SwedishTagger();
    tokenizer = new WordTokenizer();
    sentenceTokenizer = new SRXSentenceTokenizer(new Swedish());
    disambiguator = new MultiWordChunker("/sv/multiwords.txt");
  }

  @Test
  public void testChunker() throws IOException {
    // fixme! - Still missing SENT_END
    //TestTools.myAssert("blablabla","/[null]SENT_START blablabla/[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Att testa ... disambiguering",
        "/[null]SENT_START Att/[att]KN  /[null]null testa/[testa]VB:IMP|testa/[testa]VB:INF  /[null]null ./[...]<ELLIPS> ./[null]null ./[...]</ELLIPS>  /[null]null/[null]SENT_START disambiguering/[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Att testa disambiguering är, en passant, kul.",
        "/[null]SENT_START Att/[att]KN  /[null]null testa/[testa]VB:IMP|testa/[testa]VB:INF  /[null]null disambiguering/[null]null  /[null]null är/[vara]VB:PRS ,/[null]null  /[null]null en/[en passant]<NN:OF:SIN:NOM:UTR>|en/[en]NN:OF:SIN:NOM:UTR|en/[en]PN  /[null]null passant/[en passant]</NN:OF:SIN:NOM:UTR> ,/[null]null  /[null]null kul/[kul]JJ:PU ./[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);
        TestTools.myAssert("Te från Sri Lanka är mycket gott.",
        "/[null]SENT_START Te/[te]NN:OF:SIN:NOM:NEU|Te/[te]VB:IMP|Te/[te]VB:INF  /[null]null från/[från]PP  /[null]null Sri/[Sri Lanka]<PM:NOM>  /[null]null Lanka/[Sri Lanka]</PM:NOM>  /[null]null är/[vara]VB:PRS  /[null]null mycket/[mycken]JJ:PN|mycket/[mycket]AB  /[null]null gott/[god]JJ:PN|gott/[gott]AB ./[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Test ...",
        "/[null]SENT_START Test/[test]NN:OF:PLU:NOM:NEU|Test/[test]NN:OF:SIN:NOM:NEU|Test/[test]NN:OF:SIN:NOM:UTR  /[null]null ./[...]<ELLIPS> ./[null]null ./[...]</ELLIPS>", tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("Test 2 ... ",
        "/[null]SENT_START Test/[test]NN:OF:PLU:NOM:NEU|Test/[test]NN:OF:SIN:NOM:NEU|Test/[test]NN:OF:SIN:NOM:UTR  /[null]null 2/[null]null  /[null]null ./[...]<ELLIPS> ./[null]null ./[...]</ELLIPS>  /[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);

  }

}

