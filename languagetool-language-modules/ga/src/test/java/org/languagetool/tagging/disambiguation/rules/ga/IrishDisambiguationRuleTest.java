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
package org.languagetool.tagging.disambiguation.rules.ga;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Irish;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest;
import org.languagetool.tagging.ga.IrishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

public class IrishDisambiguationRuleTest extends DisambiguationRuleTest {

  private IrishTagger tagger;
  private WordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private XmlRuleDisambiguator disambiguator;

  @Before
  public void setUp() {
    tagger = new IrishTagger();
    tokenizer = new WordTokenizer();
    sentenceTokenizer = new SRXSentenceTokenizer(new Irish());
    disambiguator = new XmlRuleDisambiguator(new Irish());
  }

  @Test
  public void testDisambiguation() throws IOException {
    TestTools.myAssert("As sin amach.",
      "/[null]SENT_START As/[as]Prep:Simp  /[null]null sin/[sin]Pron:Dem  /[null]null amach/[amach]Adv:Dir ./[null]null",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("Dé Luain.",
      "/[null]SENT_START Dé/[Dé]Subst:Noun:Sg  /[null]null Luain/[Luan]Noun:Masc:Gen:Sg|Luain/[luan]Noun:Masc:Gen:Sg ./[null]null",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("á haistriú.",
      "/[null]SENT_START á/[do]Prep:Poss:3P:Sg:Fem:Obj  /[null]null haistriú/[aistriú]Verbal:Noun:VTI:hPref ./[null]null",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("a haon",
      "/[null]SENT_START a/[a]Part:Nm  /[null]null haon/[aon]Num:Card:hPref",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

}
