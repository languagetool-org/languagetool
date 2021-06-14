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
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tagging.disambiguation.ca.CatalanHybridDisambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;

public class CatalanDisambiguationRuleTest {
      
  private CatalanTagger tagger;
  private CatalanWordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private CatalanHybridDisambiguator disambiguator;

  @Before
  public void setUp() {
    tagger = CatalanTagger.INSTANCE_CAT;
    tokenizer = new CatalanWordTokenizer();
    sentenceTokenizer = new SRXSentenceTokenizer(new Catalan());
    //disambiguator = new MultiWordChunker("/ca/multiwords.txt", true);
    disambiguator = new CatalanHybridDisambiguator();
  }

  @Test
  public void testChunker() throws IOException {
    TestTools
    .myAssert(
        "Abans-d'ahir va ser",
        "/[null]SENT_START Abans-d'ahir/[Abans-d'ahir]_marca_passat|Abans-d'ahir/[abans-d'ahir]RG  /[null]null va/[anar]VAIP3S00|va/[va]_GV_  /[null]null ser/[ser]VSN00000|ser/[ser]_GV_",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "Las Palmas de Gran Canaria",
        "/[null]SENT_START Las/[Las Palmas de Gran Canaria]NPCNG00  /[null]null Palmas/[Las Palmas de Gran Canaria]NPCNG00|Palmas/[Palmas]_possible_nompropi  /[null]null de/[Las Palmas de Gran Canaria]NPCNG00  /[null]null Gran/[Gran]_possible_nompropi|Gran/[Las Palmas de Gran Canaria]NPCNG00  /[null]null Canaria/[Canaria]_possible_nompropi|Canaria/[Las Palmas de Gran Canaria]NPCNG00",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "De tal manera que no vingué.",
        "/[null]SENT_START De/[de tal manera que]LOC_CONJ  /[null]null tal/[de tal manera que]LOC_CONJ  /[null]null manera/[de tal manera que]LOC_CONJ  /[null]null que/[de tal manera que]LOC_CONJ  /[null]null no/[no]RN  /[null]null vingué/[venir]VMIS3S00 ./[.]_PUNCT",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "De tal manera ho va fer.",
        "/[null]SENT_START De/[de tal manera]LOC_ADV  /[null]null tal/[de tal manera]LOC_ADV  /[null]null manera/[de tal manera]LOC_ADV  /[null]null ho/[ho]PP3NN000  /[null]null va/[anar]VAIP3S00|va/[va]_GV_  /[null]null fer/[fer]VMN00000|fer/[fer]_GV_ ./[.]_PUNCT",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "Al capdavant del Front del Partit.",
        "/[null]SENT_START A/[al capdamunt de]LOC_PREP l/[al capdamunt de]LOC_PREP  /[null]null capdavant/[al capdamunt de]LOC_PREP  /[null]null de/[al capdamunt de]LOC_PREP l/[el]DA0MS0|l/[l]_GN_MS  /[null]null Front/[Front]_GN_MS|Front/[Front]_possible_nompropi|Front/[Front]complement|Front/[front]NCMS000  /[null]null de/[de]SPS00 l/[el]DA0MS0|l/[l]_GN_MS  /[null]null Partit/[Partit]_GN_MS|Partit/[Partit]_possible_nompropi|Partit/[Partit]complement|Partit/[partit]NCMS000 ./[.]_PUNCT",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "Hem manta vegada participat.",
        "/[null]SENT_START Hem/[haver]VAIP1P00|Hem/[haver]_GV_|Hem/[haver]_perfet  /[null]null manta/[manta vegada]LOC_ADV  /[null]null vegada/[manta vegada]LOC_ADV  /[null]null participat/[participar]VMP00SM0|participat/[participat]_GV_ ./[.]_PUNCT",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "et al.",
        "/[null]SENT_START et/[et al.]LOC_ADV  /[null]null a/[et al.]LOC_ADV l/[et al.]LOC_ADV ./[et al.]LOC_ADV",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "Al marge d'aquells",
        "/[null]SENT_START A/[al marge d']LOC_PREP l/[al marge d']LOC_PREP  /[null]null marge/[al marge d']LOC_PREP  /[null]null d'/[al marge d']LOC_PREP aquells/[aquell]DD0MP0|aquells/[aquell]PD0MP000|aquells/[aquells]complement",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "L'Aquila",
        "/[null]SENT_START L'/[L'Aquila]NPFSG00 Aquila/[Aquila]_possible_nompropi|Aquila/[L'Aquila]NPFSG00",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "Al després-dinar",
            "/[null]SENT_START A/[al després-dinar]LOC_ADV l/[al després-dinar]LOC_ADV  /[null]null després/[al després-dinar]LOC_ADV -/[al després-dinar]LOC_ADV dinar/[al després-dinar]LOC_ADV",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "d'una vegada",
            "/[null]SENT_START d'/[d'una vegada]LOC_ADV una/[d'una vegada]LOC_ADV  /[null]null vegada/[d'una vegada]LOC_ADV",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "D'una vegada",
            "/[null]SENT_START D'/[d'una vegada]LOC_ADV una/[d'una vegada]LOC_ADV  /[null]null vegada/[d'una vegada]LOC_ADV",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "Puerta del Sol",
            "/[null]SENT_START Puerta/[Puerta del Sol]NPFSG00  /[null]null de/[Puerta del Sol]NPFSG00 l/[Puerta del Sol]NPFSG00  /[null]null Sol/[Puerta del Sol]NPFSG00|Sol/[Sol]_possible_nompropi",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "A costa d'ell",
            "/[null]SENT_START A/[a costa d']LOC_PREP  /[null]null costa/[a costa d']LOC_PREP  /[null]null d'/[a costa d']LOC_PREP ell/[ell]PP3MS000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
        .myAssert(
            "A costa d’ell",
            "/[null]SENT_START A/[a costa d']LOC_PREP  /[null]null costa/[a costa d']LOC_PREP  /[null]null d'/[a costa d']LOC_PREP ell/[ell]PP3MS000",
            tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools
    .myAssert(
        "Els fons voltor",
        "/[null]SENT_START Els/[Els]_GN_MP|Els/[Els]_GN_MP|Els/[el]DA0MP0  /[null]null fons/[fons]NCMP000|fons/[fons]_GN_MP|fons/[fons]_GN_MP|fons/[fons]_GN_MP  /[null]null voltor/[fons voltor]AQ0MN0|voltor/[voltor]_GN_MP|voltor/[voltor]_GN_MP|voltor/[voltor]ignore_concordance",
        tokenizer, sentenceTokenizer, tagger, disambiguator);    
    TestTools
    .myAssert(
        "La crème de la crème",
        "/[null]SENT_START La/[La]_GN_FS|La/[La]_GN_FS|La/[el]DA0FS0  /[null]null crème/[crème de la crème]NCFS000|crème/[crème]_GN_FS|crème/[crème]_GN_FS|crème/[crème]_GN_FS  /[null]null de/[crème de la crème]AQ0FS0|de/[de]_GN_FS|de/[de]_GN_FS|de/[de]ignore_concordance  /[null]null la/[crème de la crème]AQ0FS0|la/[la]_GN_FS|la/[la]ignore_concordance  /[null]null crème/[crème de la crème]AQ0FS0|crème/[crème]_GN_FS",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

  }
}
