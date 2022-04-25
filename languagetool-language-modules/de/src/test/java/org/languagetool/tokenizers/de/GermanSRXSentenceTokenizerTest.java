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
package org.languagetool.tokenizers.de;

import org.junit.Test;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Daniel Naber
 */
public class GermanSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(Languages.getLanguageForShortCode("de-DE"));

  @Test
  public void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Dies ist ein Satz.");
    testSplit("Dies ist ein Satz. ", "Noch einer.");
    testSplit("Dies ist ein Satz.¹ ", "Noch einer.");
    testSplit("Ein Satz! ", "Noch einer.");
    testSplit("Ein Satz... ", "Noch einer.");
    testSplit("Unter http://www.test.de gibt es eine Website.");
    testSplit("Das Schreiben ist auf den 3.10. datiert.");
    testSplit("Das Schreiben ist auf den 31.1. datiert.");
    testSplit("Das Schreiben ist auf den 3.10.2000 datiert.");
    testSplit("Natürliche Vererbungsprozesse prägten sich erst im 18. und frühen 19. Jahrhundert aus.");
    testSplit("Das ist ja 1a. ", "Und das auch.");
    testSplit("Hallo, ich bin’s. ", "Könntest du kommen?");
    testSplit("In der 1. Bundesliga kam es zum Eklat.");
    testSplit("Dies ist, z. B., ein Satz."); 

    testSplit("Friedrich I., auch bekannt als Friedrich der Große.");
    testSplit("Friedrich II., auch bekannt als Friedrich der Große.");
    testSplit("Friedrich IIXC., auch bekannt als Friedrich der Große.");
    testSplit("Friedrich II. öfter auch bekannt als Friedrich der Große.");
    testSplit("Friedrich VII. öfter auch bekannt als Friedrich der Große.");
    testSplit("Friedrich X. öfter auch bekannt als Friedrich der Zehnte.");

    // non-breaking space, happen e.g. in online editors, because HTML merges spaces:
    assertThat(stokenizer.tokenize("Dies ist ein Satz. \u00A0Noch einer.").size(), is(2));
    assertThat(stokenizer.tokenize("Dies ist ein Satz.   \u00A0Noch einer.").size(), is(2));
    assertThat(stokenizer.tokenize("Dies ist ein Satz.\u00A0 Noch einer.").size(), is(2));
    assertThat(stokenizer.tokenize("Dies ist ein Satz.\u00A0\u00A0\u00A0 Noch einer.").size(), is(2));
    testSplit("Ein Satz!\u00A0", "Noch einer.");
    testSplit("Dies ist, z.\u00A0B., ein Satz.");
    testSplit("Hier steht was mit mehr Wörtern, weil wir mal sehen wollen, wie denn so die Erkennung der Satzlänge geht, wenn die Sätze doch deutlich länger werden, also wirklich deutlich länger als das normal.\u00A0", "Hier steht etwas anderes.");

    testSplit("Heute ist der 13.12.2004.");
    testSplit("Heute ist der 13. Dezember.");
    testSplit("Heute ist der 1. Januar.");
    testSplit("Es geht am 24.09. los.");
    testSplit("Es geht um ca. 17:00 los.");
    testSplit("Das in Punkt 3.9.1 genannte Verhalten.");

    testSplit("Diese Periode begann im 13. Jahrhundert und damit bla.");
    testSplit("Diese Periode begann im 13. oder 14. Jahrhundert und damit bla.");
    testSplit("Diese Periode datiert auf das 13. bis zum 14. Jahrhundert und damit bla.");

    testSplit("Das gilt lt. aktuellem Plan.");
    testSplit("Orangen, Äpfel etc. werden gekauft.");

    testSplit("Das ist,, also ob es bla.");
    testSplit("Das ist es.. ", "So geht es weiter.");

    testSplit("Das hier ist ein(!) Satz.");
    testSplit("Das hier ist ein(!!) Satz.");
    testSplit("Das hier ist ein(?) Satz.");
    testSplit("Das hier ist ein(???) Satz.");
    testSplit("Das hier ist ein(???) Satz.");

    testSplit("»Der Papagei ist grün.« ", "Das kam so.");
    testSplit("»Der Papagei ist grün«, sagte er");

    // TODO: derzeit unterscheiden wir nicht, ob nach dem Doppelpunkt ein
    // ganzer Satz kommt oder nicht:
    testSplit("Das war es: gar nichts.");
    testSplit("Das war es: Dies ist ein neuer Satz.");
    
    // Tests created as part of regression testing of SRX tokenizer. 
    // They come from Schuld und Sühne (Crime and Punishment) book. 
    testSplit("schlug er die Richtung nach der K … brücke ein. ");
    testSplit("sobald ich es von einem Freunde zurückbekomme …« Er wurde verlegen und schwieg.");
 //   testSplit(new String[] { "Verstehen Sie wohl? ", "… ", "Gestatten Sie mir noch die Frage" });
    testSplit("Er kannte eine Unmenge Quellen, aus denen er schöpfen konnte, d. h. natürlich, wo er durch Arbeit sich etwas verdienen konnte.");
    testSplit("Stimme am lautesten heraustönte …. ", "Sobald er auf der Straße war");
//    testSplit(new String[] { "Aber nein doch, er hörte alles nur zu deutlich! ", "\n", "… ", "›Also, wenn's so ist" });
    testSplit("»Welche Wohnung?\" ", "»Die, wo wir arbeiten.");
    testSplit("»Nun also, wie ist's?« ", "fragte Lushin und blickte sie fest an.");
    testSplit("»Nun also, wie ist es?« ", "fragte Lushin und blickte sie fest an.");
//    testSplit(new String[] { "gezeigt hat.« ", "… ", "Hm! " });

    testSplit("Dies ist ein Satz mit einer EMail.Addresse@example.com!");
    testSplit("Sonderbarerweise sind auch Beispiel!Eins@example.com und Foo?Bar@example.com valide.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
