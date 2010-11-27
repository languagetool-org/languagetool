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

package de.danielnaber.languagetool.tokenizers.sk;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;

public class SlovakSentenceTokenizerTest extends TestCase {

  // accept \n as paragraph:
  private SentenceTokenizer stokenizer = new SRXSentenceTokenizer("sk");
  // accept only \n\n as paragraph:
  private SentenceTokenizer stokenizer2 = new SRXSentenceTokenizer("sk");

  public final void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);
    stokenizer2.setSingleLineBreaksMarksParagraph(false);
  }

  public final void testTokenize() {
    
    testSplit(new String[] { "This is a sentence. " });
    
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit(new String[] { "Dies ist ein Satz." });
    testSplit(new String[] { "Dies ist ein Satz. ", "Noch einer." });
    testSplit(new String[] { "Ein Satz! ", "Noch einer." });
    testSplit(new String[] { "Ein Satz... ", "Noch einer." });
    testSplit(new String[] { "Unter http://www.test.de gibt es eine Website." });

	 testSplit(new String[] { "Das ist,, also ob es bla." });
    testSplit(new String[] { "Das ist es.. ", "So geht es weiter." });

    testSplit(new String[] { "Das hier ist ein(!) Satz." });
    testSplit(new String[] { "Das hier ist ein(!!) Satz." });
    testSplit(new String[] { "Das hier ist ein(?) Satz." });
    testSplit(new String[] { "Das hier ist ein(???) Satz." });
    testSplit(new String[] { "Das hier ist ein(???) Satz." });

	 // TODO: derzeit unterscheiden wir nicht, ob nach dem Doppelpunkt ein
    // ganzer Satz kommt oder nicht:
    testSplit(new String[] { "Das war es: gar nichts." });
    testSplit(new String[] { "Das war es: Dies ist ein neuer Satz." });

    // incomplete sentences, need to work for on-thy-fly checking of texts:
    testSplit(new String[] { "Here's a" });
    testSplit(new String[] { "Here's a sentence. ",
        "And here's one that's not comp" });

	 testSplit(new String[] { "„Prezydent jest niemądry”. ", "Tak wyszło." });
    testSplit(new String[] { "„Prezydent jest niemądry”, powiedział premier" });

    testSplit(new String[] { "Das Schreiben ist auf den 3.10. datiert." });
    testSplit(new String[] { "Das Schreiben ist auf den 31.1. datiert." });
    testSplit(new String[] { "Das Schreiben ist auf den 3.10.2000 datiert." });
    testSplit(new String[] { "Toto 2. vydanie bolo rozobrané za 1,5 roka." });
    testSplit(new String[] { "Festival Bažant Pohoda slávi svoje 10. výročie." });   
	 testSplit(new String[] { "Dlho odkladané parlamentné voľby v Angole sa uskutočnia 5. septembra." }); 
    testSplit(new String[] { "Das in Punkt 3.9.1 genannte Verhalten." });

	 // From the abbreviation list:
    testSplit(new String[] { "Aké sú skutočné príčiny tzv. transformačných príznakov?" });
    testSplit(new String[] { "Aké príplatky zamestnancovi (napr. za nadčas) stanovuje Zákonník práce?" });
	 testSplit(new String[] { "Počas neprítomnosti zastupuje MUDr. Marianna Krupšová." });
	 testSplit(new String[] { "Staroveký Egypt vznikol okolo r. 3150 p.n.l. (tzn. 3150 pred Kr.). ",
        "A zanikol v r. 31 pr. Kr." });
	
	 // from user bug reports:
    testSplit(new String[] { "Temperatura wody w systemie wynosi 30°C.",
        "W skład obiegu otwartego wchodzi zbiornik i armatura." });
    testSplit(new String[] { "Zabudowano kolumny o długości 45 m. ",
        "Woda z ujęcia jest dostarczana do zakładu." });
    
    // two-letter initials:
    testSplit(new String[] { "Najlepszym polskim reżyserem był St. Różewicz. ", "Chodzi o brata wielkiego poety." });
	 testSplit(new String[] { "Nore M. hrozí za podvod 10 až 15 rokov." }); 
	 testSplit(new String[] { "To jest zmienna A.", "Zaś to jest zmienna B." });   
	 // Numbers with dots.
    testSplit(new String[] { "Mam w magazynie dwie skrzynie LMD20. ", "Jestem żołnierzem i wiem, jak można ich użyć"});
    // ellipsis
    testSplit(new String[] { "Rytmem tej wiecznie przemijającej światowej egzystencji […] rytmem mesjańskiej natury jest szczęście." });
    

    // Tests taken from LanguageTool's SentenceSplitterTest.py:
    testSplit(new String[] { "This is a sentence. " });
    testSplit(new String[] { "This is a sentence. ", "And this is another one." });
    testSplit(new String[] { "This is a sentence.", "Isn't it?", "Yes, it is." });

    testSplit(new String[] { "Don't split strings like U. S. A. either." });
    testSplit(new String[] { "Don't split strings like U.S.A. either." });
    testSplit(new String[] { "Don't split... ", "Well you know. ",
        "Here comes more text." });
    testSplit(new String[] { "Don't split... well you know. ",
        "Here comes more text." });
    testSplit(new String[] { "The \".\" should not be a delimiter in quotes." });
    testSplit(new String[] { "\"Here he comes!\" she said." });
    testSplit(new String[] { "\"Here he comes!\", she said." });
    testSplit(new String[] { "\"Here he comes.\" ",
        "But this is another sentence." });
    testSplit(new String[] { "\"Here he comes!\". ", "That's what he said." });
    testSplit(new String[] { "The sentence ends here. ", "(Another sentence.)" });
    // known to fail:
    // testSplit(new String[]{"He won't. ", "Really."});
    testSplit(new String[] { "He won't go. ", "Really." });
    testSplit(new String[] { "He won't say no.", "Not really." });
    testSplit(new String[] { "This is it: a test." });
    // one/two returns = paragraph = new sentence:
    TestTools
        .testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\n", "Really." }, stokenizer);
    TestTools
        .testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\nReally." }, stokenizer2);
    // Missing space after sentence end:
    testSplit(new String[] { "James is from the Ireland!",
        "He lives in Spain now." });
  }

  private final void testSplit(final String[] sentences) {
    TestTools.testSplit(sentences, stokenizer2);
  }

}
