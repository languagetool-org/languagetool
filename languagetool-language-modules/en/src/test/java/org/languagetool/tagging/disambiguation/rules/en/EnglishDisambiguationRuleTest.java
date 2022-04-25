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
package org.languagetool.tagging.disambiguation.rules.en;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.English;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.en.EnglishHybridDisambiguator;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

public class EnglishDisambiguationRuleTest {
  
  private EnglishTagger tagger;
  private WordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private XmlRuleDisambiguator disambiguator;
  private DemoDisambiguator disamb2;
  private EnglishHybridDisambiguator hybridDisam;
  
  @Before
  public void setUp() {
    tagger = new EnglishTagger();
    tokenizer = new WordTokenizer();
    sentenceTokenizer = new SRXSentenceTokenizer(new English());
    disambiguator = new XmlRuleDisambiguator(new English());
    disamb2 = new DemoDisambiguator(); 
    hybridDisam = new EnglishHybridDisambiguator();
  }

  @Test
  public void testChunker() throws IOException {
    TestTools.myAssert("I cannot have it.",
      "/[null]SENT_START I/[I]PRP|I/[I]PRP_S1S  /[null]null cannot/[can]MD  /[null]null have/[have]VB  /[null]null it/[it]PRP|it/[it]PRP_O3SN|it/[it]PRP_S3SN ./[.]PCT",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("I cannot have it.",
      "/[null]SENT_START I/[I]PRP|I/[I]PRP_S1S  /[null]null cannot/[can]MD  /[null]null have/[have]NN|have/[have]VB|have/[have]VBP  /[null]null it/[it]PRP|it/[it]PRP_O3SN|it/[it]PRP_S3SN ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("He is to blame.",
      "/[null]SENT_START He/[he]PRP|He/[he]PRP_S3SM  /[null]null is/[be]VBZ  /[null]null to/[to]IN|to/[to]TO  /[null]null blame/[blame]VB ./[.]PCT",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("He is to blame.",
      "/[null]SENT_START He/[he]PRP|He/[he]PRP_S3SM  /[null]null is/[be]VBZ  /[null]null to/[to]IN|to/[to]TO  /[null]null blame/[blame]JJ|blame/[blame]NN:UN|blame/[blame]VB|blame/[blame]VBP ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("He is well known.",
      "/[null]SENT_START He/[he]PRP|He/[he]PRP_S3SM  /[null]null is/[be]VBZ  /[null]null well/[well]JJ|well/[well]NN|well/[well]RB|well/[well]UH|well/[well]VB|well/[well]VBP  /[null]null known/[know]VBN|known/[known]NN ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("The quid pro quo.",
        "/[null]SENT_START The/[the]DT  /[null]null quid/[quid pro quo]NN  /[null]null pro/[quid pro quo]NN  /[null]null quo/[quid pro quo]NN ./[.]PCT", 
        tokenizer, sentenceTokenizer, tagger, hybridDisam);
    TestTools.myAssert("The QUID PRO QUO.",
        "/[null]SENT_START The/[the]DT  /[null]null QUID/[quid pro quo]NN  /[null]null PRO/[quid pro quo]NN  /[null]null QUO/[quid pro quo]NN ./[.]PCT", 
        tokenizer, sentenceTokenizer, tagger, hybridDisam);
  }

}


