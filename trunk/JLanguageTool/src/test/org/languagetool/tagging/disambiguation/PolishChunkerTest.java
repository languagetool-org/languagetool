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

package de.danielnaber.languagetool.tagging.disambiguation;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tagging.pl.PolishTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class PolishChunkerTest extends TestCase {
      
    private PolishTagger tagger;
    private WordTokenizer tokenizer;
    private SentenceTokenizer sentenceTokenizer;
    private MultiWordChunker disambiguator;
      
    public void setUp() {
      tagger = new PolishTagger();
      tokenizer = new WordTokenizer();
      sentenceTokenizer = new SRXSentenceTokenizer("pl"); 
      disambiguator = new MultiWordChunker("/pl/multiwords.txt");      
    }

    public void testChunker() throws IOException {
      //TestTools.myAssert("To jest duży dom.", "/[null]SENT_START To/[to]conj|To/[ten]adj:sg:nom.acc.voc:n1.n2  /[null]null jest/[być]verb:fin:sg:ter:imperf  /[null]null duży/[duży]adj:sg:nom:m:pneg  /[null]null dom/[dom]subst:sg:nom.acc:m3 ./[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      //TestTools.myAssert("Krowa pasie się na pastwisku.", "/[null]SENT_START Krowa/[krowa]subst:sg:nom:f  /[null]null pasie/[pas]subst:sg:loc.voc:m3|pasie/[paść]verb:irreg  /[null]null się/[siebie]qub  /[null]null na/[na]prep:acc.loc  /[null]null pastwisku/[pastwisko]subst:sg:dat:n+subst:sg:loc:n ./[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      //TestTools.myAssert("blablabla","/[null]SENT_START blablabla/[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("To test... dezambiguacji",
          "/[null]SENT_START To/[ten]adj:sg:acc.nom.voc:n:pos|To/[to]conj  /[null]null test/[test]subst:sg:acc.nom:m3|test/[testo]subst:pl:gen:n ./[...]<ELLIPSIS> ./[null]null ./[...]</ELLIPSIS>  /[null]null dezambiguacji/[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("On, to znaczy premier, jest niezbyt mądry",
          "/[null]SENT_START On/[on]ppron3:sg:nom:m:ter ,/[null]null  /[null]null to/[ten]adj:sg:acc.nom.voc:n:pos|to/[to znaczy]<TO_ZNACZY>|to/[to]conj  /[null]null znaczy/[to znaczy]</TO_ZNACZY>|znaczy/[znaczyć]verb:fin:sg:ter:imperf  /[null]null premier/[premier]subst:sg:nom:m1|premier/[premiera]subst:pl:gen:f ,/[null]null  /[null]null jest/[być]verb:fin:sg:ter:imperf  /[null]null niezbyt/[zbyt]adv:neg  /[null]null mądry/[mądry]adj:sg:acc:m3:pos:aff|mądry/[mądry]adj:sg:nom:m:pos:aff|mądry/[mądry]adj:sg:voc:m:pos:aff", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("Lubię go z uwagi na krótkie włosy.",
          "/[null]SENT_START Lubię/[lubić]verb:fin:sg:pri:imperf  /[null]null go/[on]ppron3:sg:acc:m:ter:nakc:npraep|go/[on]ppron3:sg:gen:m.n.n1.n2:ter:nakc:npraep  /[null]null z/[z uwagi na]<PREP:ACC>|z/[z]prep:gen.inst  /[null]null uwagi/[uwaga]subst:pl:acc.gen.nom.voc:f|uwagi/[uwaga]subst:sg:dat.gen.loc:f  /[null]null na/[na]prep:acc.loc|na/[z uwagi na]</PREP:ACC>  /[null]null krótkie/[krótki]adj:pl:acc.nom.voc:f.m2.m3.n:pos:aff|krótkie/[krótki]adj:sg:acc.nom.voc:n:pos:aff  /[null]null włosy/[włos]subst:pl:acc.nom.voc:m3|włosy/[włosy]subst:pltant:acc.nom.voc:n ./[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("Test...",
          "/[null]SENT_START Test/[test]subst:sg:acc.nom:m3|Test/[testo]subst:pl:gen:n ./[...]<ELLIPSIS> ./[null]null ./[...]</ELLIPSIS>", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("Test... ",
          "/[null]SENT_START Test/[test]subst:sg:acc.nom:m3|Test/[testo]subst:pl:gen:n ./[...]<ELLIPSIS> ./[null]null ./[...]</ELLIPSIS>  /[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);
    }

  }

