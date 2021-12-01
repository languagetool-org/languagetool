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
package org.languagetool.tokenizers.nl;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Dutch;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

/**
 * @author Daniel Naber
 * @author Adapted by R. Baars for Dutch
 * @author Pander OpenTaal added examples from Wikipedia and Taaladvies
 */
public class DutchSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Dutch());

  @Test
  public void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Dit is een zin.");
    testSplit("Dit is een zin. ", "Nog een.");
    testSplit("Een zin! ", "Nog een.");
    testSplit("‘Dat meen je niet!’ kirde Mandy."); 
    testSplit("Een zin... ", "Nog een.");
    testSplit("'En nu.. daden!' aan premier Mark Rutte.");
    testSplit("Op http://www.test.de vind je een website.");
    testSplit("De brief is op 3-10 gedateerd.");
    testSplit("De brief is op 31-1 gedateerd.");
    testSplit("De brief is op 3-10-2000 gedateerd.");

    testSplit("Vandaag is het 13-12-2004.");
    testSplit("Op 24.09 begint het.");
    testSplit("Om 17:00 begint het.");
    testSplit("In paragraaf 3.9.1 is dat beschreven.");

    testSplit("Januari jl. is dat vastgelegd.");
    testSplit("Appel en pruimen enz. werden gekocht.");
    testSplit("De afkorting n.v.t. betekent niet van toepassing.");

    testSplit("Bla et al. blah blah.");

    testSplit("Dat is,, of het is bla.");
    testSplit("Dat is het.. ", "Zo gaat het verder.");

    testSplit("Dit hier is een(!) zin.");
    testSplit("Dit hier is een(!!) zin.");
    testSplit("Dit hier is een(?) zin.");
    testSplit("Dit hier is een(???) zin.");
    testSplit("Dit hier is een(???) zin.");

    testSplit("Als voetballer wordt hij nooit een prof. ", "Maar prof. N.A.W. Th.Ch. Janssen wordt dat wel.");

    // TODO, zin na dubbele punt
    testSplit("Dat was het: helemaal niets.");
    testSplit("Dat was het: het is een nieuwe zin.");

    // https://nl.wikipedia.org/wiki/Aanhalingsteken
    testSplit("Jan zei: \"Hallo.\"");
    testSplit("Jan zei: “Hallo.”");
    testSplit("„Hallo,” zei Jan, „waar ga je naartoe?”");
    testSplit("„Gisteren”, zei Jan, „was het veel beter weer.”");
    testSplit("Jan zei: „Annette zei ‚Hallo’.”");
    testSplit("Jan zei: “Annette zei ‘Hallo’.”");
    testSplit("Jan zei: «Annette zei ‹Hallo›.»");
    testSplit("Wegens zijn „ziekte” hoefde hij niet te werken.");
    testSplit("de letter „a”");
    testSplit("het woord „beta” is afkomstig van ...");

    // http://taaladvies.net/taal/advies/vraag/11
    testSplit("De voorzitter zei: 'Ik zie hier geen been in.'");
    testSplit("De voorzitter zei: \"Ik zie hier geen been in.\"");
    testSplit("De koning zei: \"Ik herinner me nog dat iemand 'Vive la république' riep tijdens mijn eedaflegging.\"");
    testSplit("De koning zei: 'Ik herinner me nog dat iemand \"Vive la république\" riep tijdens mijn eedaflegging.'");
    testSplit("De koning zei: 'Ik herinner me nog dat iemand 'Vive la république' riep tijdens mijn eedaflegging.'");
    testSplit("Otto dacht: wat een nare verhalen hoor je toch tegenwoordig.");

    // http://taaladvies.net/taal/advies/vraag/871
    testSplit("'Ik vrees', zei Rob, 'dat de brug zal instorten.'");
    testSplit("'Ieder land', aldus minister Powell, 'moet rekenschap afleggen over de wijze waarop het zijn burgers behandelt.'");
    testSplit("'Zeg Rob,' vroeg Jolanda, 'denk jij dat de brug zal instorten?'");
    testSplit("'Deze man heeft er niets mee te maken,' aldus korpschef Jan de Vries, 'maar hij heeft momenteel geen leven.'");
    testSplit("'Ik vrees,' zei Rob, 'dat de brug zal instorten.'");
    testSplit("'Ieder land,' aldus minister Powell, 'moet rekenschap afleggen over de wijze waarop het zijn burgers behandelt.'");
    testSplit("'Zeg Rob,' vroeg Jolanda, 'denk jij dat de brug zal instorten?'");
    testSplit("'Deze man heeft er niets mee te maken,' aldus korpschef Jan de Vries, 'maar hij heeft momenteel geen leven.'");

    // http://taaladvies.net/taal/advies/vraag/872
    testSplit("Zij antwoordde: 'Ik denk niet dat ik nog langer met je om wil gaan.'");
    testSplit("Zij fluisterde iets van 'eeuwig trouw' en 'altijd blijven houden van'.");
    testSplit("'Heb je dat boek al gelezen?', vroeg hij.");
    testSplit("De auteur vermeldt: 'Deze opvatting van het vorstendom heeft lang doorgewerkt.'");

    // http://taaladvies.net/taal/advies/vraag/1557
    testSplit("'Gaat u zitten', zei zij. ", "'De dokter komt zo.'");
    testSplit("'Mijn broer woont ook in Den Haag', vertelde ze. ", "'Hij woont al een paar jaar samen.'");
    testSplit("'Je bent grappig', zei ze. ", "'Echt, ik vind je grappig.'");
    testSplit("'Is Jan thuis?', vroeg Piet. ", "'Ik wil hem wat vragen.'");
    testSplit("'Ik denk er niet over!', riep ze. ", "'Dat gaat echt te ver, hoor!'");
    testSplit("'Ik vermoed', zei Piet, 'dat Jan al wel thuis is.'");
    
    testSplit("Het is een .Net programma. ", "Of een .NEt programma.");
    testSplit("Het is een .Net-programma. ", "Of een .NEt-programma.");
    
    testSplit("SP werd in 2001 de sp.a (Socialistische Partij Anders) en heet sinds 2021 Vooruit.");
    testSplit("SP.A grijpt terug naar naam met geschiedenis: VOORUIT.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
