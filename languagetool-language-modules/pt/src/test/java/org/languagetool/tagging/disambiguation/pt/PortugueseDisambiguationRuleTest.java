/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.disambiguation.pt;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Portuguese;
//import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
//import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.disambiguation.pt.PortugueseHybridDisambiguator;
import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

public class PortugueseDisambiguationRuleTest {
  
  private PortugueseTagger tagger;
  private WordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  //private XmlRuleDisambiguator disambiguator;
  //private DemoDisambiguator disamb2;
  private PortugueseHybridDisambiguator hybridDisam;
  
  @Before
  public void setUp() {
    tagger = new PortugueseTagger();
    tokenizer = new WordTokenizer();
    sentenceTokenizer = new SRXSentenceTokenizer(new Portuguese());
    //disambiguator = new XmlRuleDisambiguator(new Portuguese());
    //disamb2 = new DemoDisambiguator(); 
    hybridDisam = new PortugueseHybridDisambiguator();
  }

  @Test
  public void testChunker() throws IOException {
    TestTools.myAssert("A cada semana.",
        "/[null]SENT_START A/[a cada semana]RG  /[null]null cada/[a cada semana]RG  /[null]null semana/[a cada semana]RG ./[.]_PUNCT|./[.]_PUNCT_PERIOD", 
        tokenizer, sentenceTokenizer, tagger, hybridDisam);
    TestTools.myAssert("Estes são os meus amigos.",
              "/[null]SENT_START Estes/[este]DD0MP0|Estes/[este]PD0MP000  "
            + "/[null]null são/[ser]VMIP3P0|são/[são]AQ0MS0|são/[são]NCMS000  "
            + "/[null]null os/[o]DA0MP0  /[null]null meus/[meu]DP1MPS  "
            + "/[null]null amigos/[amigo]NCMP000 ./[.]_PUNCT|./[.]_PUNCT_PERIOD", tokenizer, sentenceTokenizer, tagger, hybridDisam);
    
  }

}


