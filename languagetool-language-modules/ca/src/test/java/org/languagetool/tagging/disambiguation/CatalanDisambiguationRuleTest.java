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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;

public class CatalanDisambiguationRuleTest extends DisambiguationRuleTest {
      
  private CatalanTagger tagger;
  private CatalanWordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private MultiWordChunker disambiguator;

  @Before
  public void setUp() {
    tagger = new CatalanTagger(new Catalan());
    tokenizer = new CatalanWordTokenizer();
    sentenceTokenizer = new SRXSentenceTokenizer(new Catalan());
    disambiguator = new MultiWordChunker("/ca/multiwords.txt", true);
  }

  @Test
  public void testChunker() throws IOException {
    TestTools
    .myAssert(
        "et al.",
        "/[null]SENT_START et/[et al.]<LOC_ADV>|et/[tu]P020S000|et/[tu]PP2CS000  /[null]null a/[a]NCFS000|a/[a]SPS00 l/[litre]Y ./[et al.]</LOC_ADV>",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "Al marge d'aquells",
        "/[null]SENT_START A/[a]NCFS000|A/[a]SPS00|A/[al marge d']<LOC_PREP> l/[litre]Y  /[null]null marge/[marge]NCMS000  /[null]null d'/[al marge d']</LOC_PREP>|d'/[de]SPS00 aquells/[aquell]DD0MP0|aquells/[aquell]PD0MP000",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "L'Aquila",
        "/[null]SENT_START L'/[L'Aquila]<NPFSG00>|L'/[el]DA0CS0|L'/[ell]PP3CSA00 Aquila/[L'Aquila]</NPFSG00>",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "Al després-dinar",
            "/[null]SENT_START A/[a]NCFS000|A/[a]SPS00|A/[al després-dinar]<LOC_ADV> l/[litre]Y  /[null]null després/[desprendre]VMP00SM0|després/[després]RG -/[null]null dinar/[al després-dinar]</LOC_ADV>|dinar/[dinar]NCMS000|dinar/[dinar]VMN00000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "d'una vegada",
            "/[null]SENT_START d'/[d'una vegada]<LOC_ADV>|d'/[de]SPS00 una/[un]DI0FS0|una/[un]PI0FS000  /[null]null vegada/[d'una vegada]</LOC_ADV>|vegada/[vegada]NCFS000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "D'una vegada",
            "/[null]SENT_START D'/[d'una vegada]<LOC_ADV>|D'/[de]SPS00 una/[un]DI0FS0|una/[un]PI0FS000  /[null]null vegada/[d'una vegada]</LOC_ADV>|vegada/[vegada]NCFS000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "Puerta del Sol",
            "/[null]SENT_START Puerta/[Puerta del Sol]<NPFSG00>  /[null]null de/[de]NCFS000|de/[de]SPS00 l/[litre]Y  /[null]null Sol/[Puerta del Sol]</NPFSG00>|Sol/[Sol]NPCNSP0|Sol/[Sol]NPMSSP0|Sol/[sol]AQ0MS0|Sol/[sol]NCMS000|Sol/[solar]VMIP1S0B|Sol/[soler]VMIP3S00|Sol/[soler]VMM02S00",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "A costa d'ell",
            "/[null]SENT_START A/[a costa d']<LOC_PREP>|A/[a]NCFS000|A/[a]SPS00  /[null]null costa/[costa]NCFS000|costa/[costar]VMIP3S00|costa/[costar]VMM02S00  /[null]null d'/[a costa d']</LOC_PREP>|d'/[de]SPS00 ell/[ell]PP3MS000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "A costa d’ell",
            "/[null]SENT_START A/[a costa d']<LOC_PREP>|A/[a]NCFS000|A/[a]SPS00  /[null]null costa/[costa]NCFS000|costa/[costar]VMIP3S00|costa/[costar]VMM02S00  /[null]null d'/[a costa d']</LOC_PREP>|d'/[de]SPS00 ell/[ell]PP3MS000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
  }
}
