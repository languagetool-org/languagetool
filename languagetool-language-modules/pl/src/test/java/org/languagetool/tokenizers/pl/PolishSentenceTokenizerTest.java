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

package org.languagetool.tokenizers.pl;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class PolishSentenceTokenizerTest extends TestCase {

  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Polish());

  public final void testTokenize() {
    
    testSplit(new String[] { "To się wydarzyło 3.10.2000 i mam na to dowody." });

    testSplit(new String[] { "To było 13.12 - nikt nie zapomni tego przemówienia." });    
    testSplit(new String[] { "Heute ist der 13.12.2004." });
    testSplit(new String[] { "To jest np. ten debil spod jedynki." });
    testSplit(new String[] { "To jest 1. wydanie." });
    testSplit(new String[] { "Dziś jest 13. rocznica powstania wąchockiego." });    
 
    testSplit(new String[] { "Das in Punkt 3.9.1 genannte Verhalten." });

    testSplit(new String[] { "To jest tzw. premier." });
    testSplit(new String[] { "Jarek kupił sobie kurteczkę, tj. strój Marka." });

    testSplit(new String[] { "„Prezydent jest niemądry”. ", "Tak wyszło." });
    testSplit(new String[] { "„Prezydent jest niemądry”, powiedział premier" });

    // from user bug reports:
    testSplit(new String[] { "Temperatura wody w systemie wynosi 30°C.",
        "W skład obiegu otwartego wchodzi zbiornik i armatura." });
    testSplit(new String[] { "Zabudowano kolumny o długości 45 m. ",
        "Woda z ujęcia jest dostarczana do zakładu." });
    
    // two-letter initials:
    testSplit(new String[] { "Najlepszym polskim reżyserem był St. Różewicz. ", "Chodzi o brata wielkiego poety." });
    
    // From the abbreviation list:
    testSplit(new String[] { "Ks. Jankowski jest prof. teologii." });
    testSplit(new String[] { "To wydarzyło się w 1939 r.",
        "To był burzliwy rok." });
    testSplit(new String[] { "Prezydent jest popierany przez 20 proc. społeczeństwa." });
    testSplit(new String[] {
        "Moje wystąpienie ma na celu zmobilizowanie zarządu partii do działań, które umożliwią uzyskanie 40 proc.",
        "Nie widzę dziś na scenie politycznej formacji, która lepiej by łączyła różne poglądy" });
    testSplit(new String[] { "To jest zmienna A.", "Zaś to jest zmienna B." });
    // SKROTY_BEZ_KROPKI in ENDABREVLIST
    testSplit(new String[] { "Mam już 20 mln.", "To powinno mi wystarczyć" });
    testSplit(new String[] { "Mam już 20 mln. buraków." });
    // ellipsis
    testSplit(new String[] { "Rytmem tej wiecznie przemijającej światowej egzystencji […] rytmem mesjańskiej natury jest szczęście." });
    // sic!
    testSplit(new String[] { "W gazecie napisali, że pasy (sic!) pogryzły człowieka." });
    // Numbers with dots.
    testSplit(new String[] { "Mam w magazynie dwie skrzynie LMD20. ", "Jestem żołnierzem i wiem, jak można ich użyć"});
  }

  private void testSplit(final String[] sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
