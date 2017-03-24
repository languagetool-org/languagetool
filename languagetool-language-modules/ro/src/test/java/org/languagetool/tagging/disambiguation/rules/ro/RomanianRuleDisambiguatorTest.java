/* LanguageTool, a natural language style checker
 * Copyright (C) 2009 Marcin Miłkowski
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
package org.languagetool.tagging.disambiguation.rules.ro;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Romanian;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.ro.RomanianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.ro.RomanianWordTokenizer;

import java.io.IOException;

public class RomanianRuleDisambiguatorTest {

  private RomanianTagger tagger;
  private RomanianWordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private XmlRuleDisambiguator disambiguator;
  private DemoDisambiguator disamb2;

  @Before
  public void setUp() {
    tagger = new RomanianTagger();
    tokenizer = new RomanianWordTokenizer();
    Romanian language = new Romanian();
    sentenceTokenizer = new SRXSentenceTokenizer(language);
    disambiguator = new XmlRuleDisambiguator(language);
    disamb2 = new DemoDisambiguator();
  }

  @Test
  public void testCare1() throws IOException {
    TestTools
            .myAssert(
                    "Persoana care face treabă.",
                    "/[null]SENT_START Persoana/[persoană]Sfs3aac000  /[null]null care/[car]Snp3anc000|care/[care]0000000000|care/[care]N000a0l000|care/[căra]V0p3000cz0|care/[căra]V0s3000cz0  /[null]null face/[face]V000000f00|face/[face]V0s3000iz0  /[null]null treabă/[treabă]Sfs3anc000 ./[null]null",
                    tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools
            .myAssert(
                    "Persoana care face treabă.",
                    "/[null]SENT_START Persoana/[persoană]Sfs3aac000  /[null]null care/[care]N000a0l000  /[null]null face/[face]V000000f00|face/[face]V0s3000iz0  /[null]null treabă/[treabă]Sfs3anc000 ./[null]null",
                    tokenizer, sentenceTokenizer, tagger, disambiguator);

  }

  @Test
  public void testEsteO() throws IOException {
    TestTools
            .myAssert(
                    "este o masă.",
                    "/[null]SENT_START este/[fi]V0s3000izb  /[null]null o/[o]Dfs3a0t000|o/[o]I00000o000|o/[o]Nfs3a0p00c|o/[o]Sms3anc000|o/[vrea]V0s3000iov  /[null]null masă/[masa]V0s3000is0|masă/[masă]Sfs3anc000 ./[null]null",
                    tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools
            .myAssert(
                    "este o masă.",
                    "/[null]SENT_START este/[fi]V0s3000izb  /[null]null o/[o]Dfs3a0t000|o/[o]I00000o000|o/[o]Nfs3a0p00c|o/[o]Sms3anc000|o/[vrea]V0s3000iov  /[null]null masă/[masă]Sfs3anc000 ./[null]null",
                    tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
            .myAssert(
                    "este o masă",
                    "/[null]SENT_START este/[fi]V0s3000izb  /[null]null o/[o]Dfs3a0t000|o/[o]I00000o000|o/[o]Nfs3a0p00c|o/[o]Sms3anc000|o/[vrea]V0s3000iov  /[null]null masă/[masă]Sfs3anc000",
                    tokenizer, sentenceTokenizer, tagger, disambiguator);

  }

  @Test
  public void testDezambiguizareVerb() throws IOException {
    TestTools
            .myAssert(
                    "vom participa la",
                    "/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null participa/[participa]V000000f00|participa/[participa]V0s3000ii0  /[null]null la/[la]P000000000|la/[la]Sms3anc000",
                    tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools
            .myAssert(
                    "vom participa la",
                    "/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null participa/[participa]V000000f00  /[null]null la/[la]P000000000|la/[la]Sms3anc000",
                    tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools
            .myAssert(
                    "vom culege",
                    "/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null culege/[culege]V000000f00|culege/[culege]V0s2000m00|culege/[culege]V0s3000iz0",
                    tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools
            .myAssert(
                    "vom culege",
                    "/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null culege/[culege]V000000f00",
                    tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
            .myAssert(
                    "veți culege",
                    "/[null]SENT_START veți/[vrea]V0p2000ivv  /[null]null culege/[culege]V000000f00",
                    tokenizer, sentenceTokenizer, tagger, disambiguator);
  }
}
