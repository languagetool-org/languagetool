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
package de.danielnaber.languagetool.tokenizers.de;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;

/**
 * @author Daniel Naber
 */
public class GermanSentenceTokenizerTest extends TestCase {

  // accept \n as paragraph:
  private GermanSentenceTokenizer stokenizer = new GermanSentenceTokenizer();
  // accept only \n\n as paragraph:
  private GermanSentenceTokenizer stokenizer2 = new GermanSentenceTokenizer();
  
  public void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);  
    stokenizer2.setSingleLineBreaksMarksParagraph(false);  
  }

  public void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit(new String[] { "Dies ist ein Satz." });
    testSplit(new String[] { "Dies ist ein Satz. ", "Noch einer." });
    testSplit(new String[] { "Ein Satz! ", "Noch einer." });
    testSplit(new String[] { "Ein Satz... ", "Noch einer." });
    testSplit(new String[] { "Unter http://www.test.de gibt es eine Website." });
    testSplit(new String[] { "Das Schreiben ist auf den 3.10. datiert." });
    testSplit(new String[] { "Das Schreiben ist auf den 31.1. datiert." });
    testSplit(new String[] { "Das Schreiben ist auf den 3.10.2000 datiert." });
    testSplit(new String[] { "Natürliche Vererbungsprozesse prägten sich erst im 18. und frühen 19. Jahrhundert aus." });

    testSplit(new String[] { "Friedrich I., auch bekannt als Friedrich der Große." });
    testSplit(new String[] { "Friedrich II., auch bekannt als Friedrich der Große." });
    testSplit(new String[] { "Friedrich IIXC., auch bekannt als Friedrich der Große." });
    testSplit(new String[] { "Friedrich II. öfter auch bekannt als Friedrich der Große." });
    testSplit(new String[] { "Friedrich VII. öfter auch bekannt als Friedrich der Große." });
    testSplit(new String[] { "Friedrich X. öfter auch bekannt als Friedrich der Zehnte." });

    testSplit(new String[] { "Heute ist der 13.12.2004." });
    testSplit(new String[] { "Heute ist der 13. Dezember." });
    testSplit(new String[] { "Heute ist der 1. Januar." });
    testSplit(new String[] { "Es geht am 24.09. los." });
    testSplit(new String[] { "Es geht um ca. 17:00 los." });
    testSplit(new String[] { "Das in Punkt 3.9.1 genannte Verhalten." });

    testSplit(new String[] { "Diese Periode begann im 13. Jahrhundert und damit bla." });
    testSplit(new String[] { "Diese Periode begann im 13. oder 14. Jahrhundert und damit bla." });
    testSplit(new String[] { "Diese Periode datiert auf das 13. bis zum 14. Jahrhundert und damit bla." });

    testSplit(new String[] { "Das gilt lt. aktuellem Plan." });
    testSplit(new String[] { "Orangen, Äpfel etc. werden gekauft." });

    testSplit(new String[] { "Das ist,, also ob es bla." });
    testSplit(new String[] { "Das ist es.. ", "So geht es weiter." });

    testSplit(new String[] { "Das hier ist ein(!) Satz." });
    testSplit(new String[] { "Das hier ist ein(!!) Satz." });
    testSplit(new String[] { "Das hier ist ein(?) Satz." });
    testSplit(new String[] { "Das hier ist ein(???) Satz." });
    testSplit(new String[] { "Das hier ist ein(???) Satz." });

    testSplit(new String[] { "»Der Papagei ist grün.« ", "Das kam so." });
    testSplit(new String[] { "»Der Papagei ist grün«, sagte er" });

    // TODO: derzeit unterscheiden wir nicht, ob nach dem Doppelpunkt ein
    // ganzer Satz kommt oder nicht:
    testSplit(new String[] { "Das war es: gar nichts." });
    testSplit(new String[] { "Das war es: Dies ist ein neuer Satz." });
  }

  public void testSplit(String[] sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
