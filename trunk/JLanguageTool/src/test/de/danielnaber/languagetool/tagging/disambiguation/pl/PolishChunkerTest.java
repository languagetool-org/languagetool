package de.danielnaber.languagetool.tagging.disambiguation.pl;

import junit.framework.TestCase;
import java.io.IOException;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;
import de.danielnaber.languagetool.tokenizers.pl.PolishSentenceTokenizer;
import de.danielnaber.languagetool.tagging.disambiguation.pl.PolishChunker;
import de.danielnaber.languagetool.tagging.pl.*;

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


public class PolishChunkerTest extends TestCase {
      
    private PolishTagger tagger;
    private WordTokenizer tokenizer;
    private PolishSentenceTokenizer sentenceTokenizer;
    private PolishChunker disambiguator;
      
    public void setUp() {
      tagger = new PolishTagger();
      tokenizer = new WordTokenizer();
      sentenceTokenizer = new PolishSentenceTokenizer();
      disambiguator = new PolishChunker();      
    }

    public void testChunker() throws IOException {
      TestTools.myAssert("To jest duży dom.", "/[null]SENT_START To/[to]conj|To/[ten]adj:sg:nom.acc.voc:n1.n2  /[null]null jest/[być]verb:fin:sg:ter:imperf  /[null]null duży/[duży]adj:sg:nom:m:pneg  /[null]null dom/[dom]subst:sg:nom.acc:m3 ./[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("Krowa pasie się na pastwisku.", "/[null]SENT_START Krowa/[krowa]subst:sg:nom:f  /[null]null pasie/[pas]subst:sg:loc.voc:m3|pasie/[paść]verb:irreg  /[null]null się/[siebie]qub  /[null]null na/[na]prep:acc.loc  /[null]null pastwisku/[pastwisko]subst:sg:dat:n+subst:sg:loc:n ./[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("blablabla","/[null]SENT_START blablabla/[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("To jest test... dezambiguacji","/[null]SENT_START To/[to]conj|To/[ten]adj:sg:nom.acc.voc:n1.n2  /[null]null jest/[być]verb:fin:sg:ter:imperf  /[null]null test/[test]subst:sg:nom.acc:m3|test/[testo]subst:pl:gen:n ./[...]<ELLIPSIS> ./[null]null ./[...]</ELLIPSIS>  /[null]null dezambiguacji/[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
      TestTools.myAssert("To jest test.\n\n Drugie zdanie.","/[null]SENT_START To/[to]conj|To/[ten]adj:sg:nom.acc.voc:n1.n2  /[null]null jest/[być]verb:fin:sg:ter:imperf  /[null]null test/[test]subst:sg:nom.acc:m3|test/[testo]subst:pl:gen:n ./[null]null \n/[null]SENT_END/[null]SENT_START \n/[null]SENT_END|\n/[null]PARA_END/[null]SENT_START  /[null]null Drugie/[drugi]adj:sg:nom.acc.voc:n:pneg+adj:pl:acc.nom.voc:f.n:pneg  /[null]null zdanie/[zdanie]subst:sg:nom.acc:n|zdanie/[zdać]subst:ger:sg:nom.acc.voc:n ./[null]SENT_END", tokenizer, sentenceTokenizer, tagger, disambiguator);
    }

  }

