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
import org.languagetool.language.Polish;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest;
import org.languagetool.tagging.pl.PolishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

public class PolishDisambiguationRuleTest extends DisambiguationRuleTest {
      
    private PolishTagger tagger;
    private WordTokenizer tokenizer;
    private SentenceTokenizer sentenceTokenizer;
    private MultiWordChunker disambiguator;
      
    @Before
    public void setUp() {
      tagger = new PolishTagger();
      tokenizer = new WordTokenizer();
      sentenceTokenizer = new SRXSentenceTokenizer(new Polish());
      disambiguator = new MultiWordChunker("/pl/multiwords.txt");
    }

    @Test
    public void testChunker() throws IOException {
      //TestTools.myAssert("To jest duży dom.", "/[null]SENT_START To/[to]conj|To/[ten]adj:sg:nom.acc.voc:n1.n2  /[null]null jest/[być]verb:fin:sg:ter:imperf  /[null]null duży/[duży]adj:sg:nom:m:pneg  /[null]null dom/[dom]subst:sg:nom.acc:m3 ./[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      //TestTools.myAssert("Krowa pasie się na pastwisku.", "/[null]SENT_START Krowa/[krowa]subst:sg:nom:f  /[null]null pasie/[pas]subst:sg:loc.voc:m3|pasie/[paść]verb:irreg  /[null]null się/[siebie]qub  /[null]null na/[na]prep:acc.loc  /[null]null pastwisku/[pastwisko]subst:sg:dat:n+subst:sg:loc:n ./[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      //TestTools.myAssert("blablabla","/[null]SENT_START blablabla/[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("To test... dezambiguacji",
          "/[null]SENT_START To/[ten]adj:sg:acc:n1.n2:pos|To/[ten]adj:sg:nom.voc:n1.n2:pos|To/[to]conj|To/[to]qub|To/[to]subst:sg:acc:n2|To/[to]subst:sg:nom:n2  /[null]null test/[test]subst:sg:acc:m3|test/[test]subst:sg:nom:m3 ./[...]<ELLIPSIS> ./[null]null ./[...]</ELLIPSIS>  /[null]null dezambiguacji/[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("On, to znaczy premier, jest niezbyt mądry",
          "/[null]SENT_START On/[on]adj:sg:acc:m3:pos|On/[on]adj:sg:nom.voc:m1.m2.m3:pos|On/[on]ppron3:sg:nom:m1.m2.m3:ter:akc.nakc:praep.npraep ,/[null]null  /[null]null to/[ten]adj:sg:acc:n1.n2:pos|to/[ten]adj:sg:nom.voc:n1.n2:pos|to/[to znaczy]<TO_ZNACZY>|to/[to]conj|to/[to]qub|to/[to]subst:sg:acc:n2|to/[to]subst:sg:nom:n2  /[null]null znaczy/[to znaczy]</TO_ZNACZY>|znaczy/[znaczyć]verb:fin:sg:ter:imperf:refl.nonrefl  /[null]null premier/[premier]subst:pl:acc:f|premier/[premier]subst:pl:dat:f|premier/[premier]subst:pl:gen:f|premier/[premier]subst:pl:inst:f|premier/[premier]subst:pl:loc:f|premier/[premier]subst:pl:nom:f|premier/[premier]subst:pl:voc:f|premier/[premier]subst:sg:acc:f|premier/[premier]subst:sg:dat:f|premier/[premier]subst:sg:gen:f|premier/[premier]subst:sg:inst:f|premier/[premier]subst:sg:loc:f|premier/[premier]subst:sg:nom:f|premier/[premier]subst:sg:nom:m1|premier/[premier]subst:sg:voc:f|premier/[premiera]subst:pl:gen:f ,/[null]null  /[null]null jest/[być]verb:fin:sg:ter:imperf:nonrefl  /[null]null niezbyt/[niezbyt]adv  /[null]null mądry/[mądry]adj:sg:acc:m3:pos|mądry/[mądry]adj:sg:nom.voc:m1.m2.m3:pos|mądry/[mądry]subst:sg:nom:m1|mądry/[mądry]subst:sg:voc:m1", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("Lubię go z uwagi na krótkie włosy.",
          "/[null]SENT_START Lubię/[lubić]verb:fin:sg:pri:imperf:nonrefl|Lubię/[lubić]verb:fin:sg:pri:imperf:refl.nonrefl  /[null]null go/[go]subst:pl:acc:n2|go/[go]subst:pl:dat:n2|go/[go]subst:pl:gen:n2|go/[go]subst:pl:inst:n2|go/[go]subst:pl:loc:n2|go/[go]subst:pl:nom:n2|go/[go]subst:pl:voc:n2|go/[go]subst:sg:acc:n2|go/[go]subst:sg:dat:n2|go/[go]subst:sg:gen:n2|go/[go]subst:sg:inst:n2|go/[go]subst:sg:loc:n2|go/[go]subst:sg:nom:n2|go/[go]subst:sg:voc:n2|go/[on]ppron3:sg:acc:m1.m2.m3:ter:nakc:npraep|go/[on]ppron3:sg:gen:m1.m2.m3:ter:nakc:npraep|go/[on]ppron3:sg:gen:n1.n2:ter:nakc:npraep  /[null]null z/[z uwagi na]<PREP:ACC>|z/[z]prep:acc:nwok|z/[z]prep:gen:nwok|z/[z]prep:inst:nwok  /[null]null uwagi/[uwaga]subst:pl:acc:f|uwagi/[uwaga]subst:pl:nom:f|uwagi/[uwaga]subst:pl:voc:f|uwagi/[uwaga]subst:sg:gen:f  /[null]null na/[na]interj|na/[na]prep:acc|na/[na]prep:loc|na/[z uwagi na]</PREP:ACC>  /[null]null krótkie/[krótki]adj:pl:acc:m2.m3.f.n1.n2.p2.p3:pos|krótkie/[krótki]adj:pl:nom.voc:m2.m3.f.n1.n2.p2.p3:pos|krótkie/[krótki]adj:sg:acc:n1.n2:pos|krótkie/[krótki]adj:sg:nom.voc:n1.n2:pos  /[null]null włosy/[włos]subst:pl:acc:m3|włosy/[włos]subst:pl:nom:m3|włosy/[włos]subst:pl:voc:m3|włosy/[włosy]subst:pl:acc:p3|włosy/[włosy]subst:pl:nom:p3|włosy/[włosy]subst:pl:voc:p3 ./[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("Test...",
          "/[null]SENT_START Test/[test]subst:sg:acc:m3|Test/[test]subst:sg:nom:m3 ./[...]<ELLIPSIS> ./[null]null ./[...]</ELLIPSIS>", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("Test... ",
          "/[null]SENT_START Test/[test]subst:sg:acc:m3|Test/[test]subst:sg:nom:m3 ./[...]<ELLIPSIS> ./[null]null ./[...]</ELLIPSIS>  /[null]null", tokenizer, sentenceTokenizer, tagger, disambiguator);
    }

}
