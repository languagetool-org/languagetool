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
package org.languagetool.tagging.disambiguation;

import java.io.IOException;

import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;

public class CatalanDisambiguatorTest extends DisambiguationRuleTest {
      
    private CatalanTagger tagger;
    private CatalanWordTokenizer tokenizer;
    private SentenceTokenizer sentenceTokenizer;
    private MultiWordChunker disambiguator;
      
    @Override
    public void setUp() {
      tagger = new CatalanTagger();
      tokenizer = new CatalanWordTokenizer();
      sentenceTokenizer = new SRXSentenceTokenizer(new Catalan());
      disambiguator = new MultiWordChunker("/ca/multiwords.txt");
    }
    
  public void testChunker() throws IOException {
    TestTools
        .myAssert(
            "A costa d’ell",
            "/[null]SENT_START A/[A costa d']<LOC_PREP>|A/[a]NCFS000|A/[a]SPS00  /[null]null costa/[costa]NCFS000|costa/[costar]VMIP3S00|costa/[costar]VMM02S00  /[null]null d'/[A costa d']</LOC_PREP>|d'/[de]SPS00 ell/[ell]PP3MS000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
  }
}
