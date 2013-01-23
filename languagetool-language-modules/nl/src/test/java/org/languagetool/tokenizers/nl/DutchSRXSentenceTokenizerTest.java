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

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Dutch;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

/**
 * @author Daniel Naber
 * @author Adapted by R. Baars for Dutch * 
 */
public class DutchSRXSentenceTokenizerTest extends TestCase {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Dutch());

  public void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Dit is een zin.");
    testSplit("Dit is een zin. ", "Nog een.");
    testSplit("Een zin! ", "Nog een.");
    testSplit("Een zin... ", "Nog een.");
    testSplit("Op http://www.test.de vind je een website.");
    testSplit("De brief is op 3.10 gedateerd.");
    testSplit("De brief is op 31.1 gedateerd.");
    testSplit("De breif is op 3.10.2000 gedateerd.");

    testSplit("Vandaag is het 13.12.2004.");
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

    testSplit("»De papagaai is groen.« ", "Dat was hij al.");
    testSplit("»De papagaai is groen«, zei hij.");

    testSplit("Als voetballer wordt hij nooit een prof. ", "Maar prof. N.A.W. Th.Ch. Janssen wordt dat wel.");
    
    // TODO, zin na dubbele punt
    testSplit("Dat was het: helemaal niets.");
    testSplit("Dat was het: het is een nieuwe zin.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
