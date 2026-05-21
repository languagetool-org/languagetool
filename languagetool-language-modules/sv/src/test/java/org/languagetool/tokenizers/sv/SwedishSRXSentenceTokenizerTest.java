/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.sv;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Swedish;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class SwedishSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Swedish());

  @Test
  public void testTokenize() {
    testSplit("Onkel Toms stuga är en roman skriven av Harriet Beecher Stowe, publicerad den 1852. ",
      "Den handlar om slaveriet i USA sett ur slavarnas perspektiv och bidrog starkt till att slaveriet avskaffades 1865 efter amerikanska inbördeskriget.");
    testSplit(
      "Vi kan leverera varorna alt. snabbt med expressfrakt.",
      "Art. handlar om klimatförändringar i Norden.",
      "Bil. innehåller mer detaljerad information.",
      "Rapporten innehåller bl.a. statistik och tabeller.",
      "Han kom sent, dvs. att mötet redan hade börjat.",
      "Vi behöver hammare, spik, etc. verktyg för arbetet.",
      "Hör av dig vid ev. problem med datorn.",
      "Den f.d. kollega arbetar nu på ett nytt företag.",
      "Fig. visar sambandet mellan tid och temperatur.",
      "Berättelsen slutar med texten “forts. följer”.",
      "Avtalet gäller fr.o.m. måndag.",
      "Priset anges inkl. moms.",
      "Kol. visar medelvärden för varje grupp.",
      "Resultatet är m.a.o. fel.",
      "Orig. version sparades i arkivet.",
      "Vi diskuterade mål, strategier, osv. detaljer.",
      "Han var frånvarande p.g.a. sjukdom.",
      "Vi har satt ett prel. schema för veckan.",
      "Ref. hänvisar till tidigare forskning.",
      "De har resp. ansvar för olika områden.",
      "Han kallar sig en s.k. expert.",
      "Varje exemplar st. kostar fem kronor.",
      "Jag gillar djur, t.ex. hundar och katter.",
      "Vi tar upp övr. frågor på nästa möte."
    );
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
