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

package org.languagetool.tokenizers.cs;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.SentenceTokenizer;

public class CzechSentenceTokenizerTest extends TestCase {

  // accept \n as paragraph:
  private final SentenceTokenizer stokenizer = new CzechSentenceTokenizer();

  // accept only \n\n as paragraph:
  private final SentenceTokenizer stokenizer2 = new CzechSentenceTokenizer();

  public final void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);
    stokenizer2.setSingleLineBreaksMarksParagraph(false);
  }

  public final void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Dies ist ein Satz.");
    testSplit("Tři sta třicet tři stříbrných křepelek přeletělo přes stři sta třicet tři stříbrných střech.");
    testSplit("Dies ist ein Satz. ", "Noch einer.");
    testSplit("Ein Satz! ", "Noch einer.");
    testSplit("Ein Satz... ", "Noch einer.");
    testSplit("Unter http://www.test.de gibt es eine Website.");
    testSplit("Das Schreiben ist auf den 3.10. datiert.");
    testSplit("Das Schreiben ist auf den 31.1. datiert.");
    testSplit("Das Schreiben ist auf den 3.10.2000 datiert.");

    testSplit("Heute ist der 13.12.2004.");
    testSplit("Dnes je 16.3.2007.");
    testSplit("Tohle je 1. verze testu českého tokenizeru.");
    testSplit("Es geht am 24.09. los.");
    testSplit("Das in Punkt 3.9.1 genannte Verhalten.");

    testSplit("Das ist,, also ob es bla.");
    testSplit("Das ist es.. ", "So geht es weiter.");

    testSplit("Das hier ist ein(!) Satz.");
    testSplit("Das hier ist ein(!!) Satz.");
    testSplit("Das hier ist ein(?) Satz.");
    testSplit("Das hier ist ein(???) Satz.");
    testSplit("Das hier ist ein(???) Satz.");

    testSplit("„Česká sazba se oproti okolnímu světu v některých aspektech mírně liší”. ", "Bylo řečeno.");
    testSplit("„Jeď nejrychleji jak můžeš”, řekla mu tiše.");

    // TODO: derzeit unterscheiden wir nicht, ob nach dem Doppelpunkt ein
    // ganzer Satz kommt oder nicht:
    testSplit("Das war es: gar nichts.");
    testSplit("Das war es: Dies ist ein neuer Satz.");

    // incomplete sentences, need to work for on-thy-fly checking of texts:
    testSplit("Here's a");
    testSplit("Here's a sentence. ", "And here's one that's not comp");

    // Tests taken from LanguageTool's SentenceSplitterTest.py:
    testSplit("This is a sentence. ");
    testSplit("This is a sentence. ", "And this is another one.");
    testSplit("This is a sentence.", "Isn't it?", "Yes, it is.");

    testSplit("Don't split strings like U. S. A. either.");
    testSplit("Don't split... ", "Well you know. ", "Here comes more text.");
    testSplit("Don't split... well you know. ", "Here comes more text.");
    testSplit("The \".\" should not be a delimiter in quotes.");
    testSplit("\"Here he comes!\" she said.");
    testSplit("\"Here he comes!\", she said.");
    testSplit("\"Here he comes.\" ", "But this is another sentence.");
    testSplit("\"Here he comes!\". ", "That's what he said.");
    testSplit("The sentence ends here. ", "(Another sentence.)");
    // known to fail:
    // testSplit(new String[]{"He won't. ", "Really."});
    testSplit("He won't go. ", "Really.");
    testSplit("He won't say no.", "Not really.");
    testSplit("He won't say No.", "Not really.");
    testSplit("This is it: a test.");
    // one/two returns = paragraph = new sentence:
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\n", "Really." }, stokenizer);
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\nReally." }, stokenizer2);
    // Missing space after sentence end:
    testSplit("James is from the Ireland!", "He lives in Spain now.");
    // From the abbreviation list:
    testSplit("V češtině jsou zkr. i pro jazyky, např. angl., maď. a jiné.");
    testSplit("Titul jako doc. RNDr. Adam Řezník, Ph.D. se může vyskytnout.");
    testSplit("Starověký Egypt vznikl okolo r. 3150 př.n.l. (anebo 3150 př.kr.). ", "A zanikl v r. 31 př.kr.");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
