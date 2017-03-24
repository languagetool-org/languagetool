/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Esben Aaberg
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
package org.languagetool.tokenizers.da;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Danish;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

/**
 * @author Esben Aaberg
 */
public class DanishSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Danish());

  @Test
  public void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Dette er en sætning.");
    testSplit("Dette er en sætning. ", "Her er den næste.");
    testSplit("En sætning! ", "Yderlige en.");
    testSplit("En sætning... ", "Yderlige en.");
    testSplit("På hjemmesiden http://www.stavekontrolden.dk bygger vi stavekontrollen.");
    testSplit("Den 31.12. går ikke!");
    testSplit("Den 3.12.2011 går ikke!");
    testSplit("I det 18. og tidlige 19. århundrede hentede amerikansk kunst det meste af sin inspiration fra Europa.");

    testSplit("Hendes Majestæt Dronning Margrethe II (Margrethe Alexandrine Þórhildur Ingrid, Danmarks dronning) (født 16. april 1940 på Amalienborg Slot) er siden 14. januar 1972 Danmarks regent.");
    testSplit("Hun har residensbolig i Christian IX's Palæ på Amalienborg Slot.");
    testSplit("Tronfølgeren ledte herefter statsrådsmøderne under Kong Frederik 9.'s fravær.");
    testSplit("Marie Hvidt, Frederik IV - En letsindig alvorsmand, Gads Forlag, 2004.");
    testSplit("Da vi første gang besøgte Restaurant Chr. IV, var vi de eneste gæster.");

    testSplit("I dag er det den 25.12.2010.");
    testSplit("I dag er det d. 25.12.2010.");
    testSplit("I dag er den 13. december.");
    testSplit("Arrangementet starter ca. 17:30 i dag.");
    testSplit("Arrangementet starter ca. 17:30.");
    testSplit("Det er nævnt i punkt 3.6.4 Rygbelastende helkropsvibrationer.");

    testSplit("Rent praktisk er det også lettest lige at mødes, så der kan udveksles nøgler og brugsanvisninger etc.");
    testSplit("Andre partier incl. borgerlige partier har deres særlige problemer: nogle samarbejder med apartheidstyret i Sydafrika, med NATO-landet Tyrkiet etc., men det skal så sandelig ikke begrunde en SF-offensiv for et samarbejde med et parti.");

    testSplit("Hvad nu,, den bliver også.");
    testSplit("Det her er det.. ", "Og her fortsætter det.");

    testSplit("Dette er en(!) sætning.");
    testSplit("Dette er en(!!) sætning.");
    testSplit("Dette er en(?) sætning.");
    testSplit("Dette er en(??) sætning.");
    testSplit("Dette er en(???) sætning.");
    testSplit("Militær værnepligt blev indført (traktaten krævede, at den tyske hær ikke oversteg 100.000 mand).");

    testSplit("Siden illustrerede hun \"Historierne om Regnar Lodbrog\" 1979 og \"Bjarkemål\" 1982 samt Poul Ørums \"Komedie i Florens\" 1990.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
