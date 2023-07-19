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
package org.languagetool.tagging.sv;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Swedish;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class SwedishTaggerTest {
    
  private SwedishTagger tagger;
  private WordTokenizer tokenizer;
      
  @Before
  public void setUp() {
    tagger = new SwedishTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Swedish());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Det är nog bäst att du får en klubba till",
        "Det/[det]PN -- är/[vara]VB:PRS -- nog/[nog]AB -- bäst/[bra]JJ:S|bäst/[bäst]AB|bäst/[god]JJ:S -- att/[att]KN -- du/[du]PN -- får/[få]VB:PRS|får/[får]NN:OF:PLU:NOM:NEU|får/[får]NN:OF:SIN:NOM:NEU -- en/[en]NN:OF:SIN:NOM:UTR|en/[en]PN -- klubba/[klubba]NN:OF:SIN:NOM:UTR|klubba/[klubba]VB:IMP|klubba/[klubba]VB:INF -- till/[till]AB|till/[till]PP", tokenizer, tagger);        
    TestTools.myAssert("Du menar sannolikt \"massera\" om du inte skriver om masarnas era förstås.",
        "Du/[du]PN -- menar/[mena]VB:PRS -- sannolikt/[sannolik]JJ:PN|sannolikt/[sannolikt]AB -- massera/[massera]VB:IMP|massera/[massera]VB:INF -- om/[om]AB|om/[om]KN|om/[om]PP -- du/[du]PN -- inte/[inte]AB -- skriver/[skriva]VB:PRS -- om/[om]AB|om/[om]KN|om/[om]PP -- masarnas/[mas]NN:BF:PLU:GEN:UTR -- era/[era]NN:OF:SIN:NOM:UTR|era/[era]PN -- förstås/[förstå]VB:INF:PF|förstås/[förstå]VB:PRS:PF|förstås/[förstås]AB", tokenizer, tagger);        
  }

}
