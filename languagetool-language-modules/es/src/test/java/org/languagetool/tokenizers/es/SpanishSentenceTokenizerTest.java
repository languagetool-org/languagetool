package org.languagetool.tokenizers.es;

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


import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class SpanishSentenceTokenizerTest {

  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Spanish());

  @Test
  public final void testTokenize() {
    
    // Simple sentences
    testSplit("Esto es una frase. ", "Esto es otra frase.");
    testSplit("Esto es una frase.[34] ", "Esto es otra frase.");
    testSplit("¿Nos vamos? ", "Hay que irse.");
    testSplit("¿Vamos? ", "Hay que irse.");
    testSplit("¡Corre! ", "Hay que irse.");
    
    // Ellipsis
    testSplit("Entonces... apareció él.");
    testSplit("Entonces… apareció él.");
    testSplit("Entoncess… ", "Apareció él.");
    testSplit("«El tal del tal…» sale de aquí");
    testSplit("invitarle –cuando sea–… a tomar café.");
    
    // Initials
    testSplit("A la atención de A. Comes.");
    testSplit("A la atenció de À. Comes.");
    testSplit("Núm. operación 220130000138.");
    testSplit("N. operación 220130000138.");
    testSplit("N.º operación 220130000138.");  

    // Abbreviations
    testSplit("las Sras. diputadas");
    testSplit("No Mr. Spock sino otro.");
    testSplit("Ver el cap. 24 del libro.");
    testSplit("Ver el cap. IX del libro.");
    testSplit("Vive en el núm. 24 de la calle.");
    testSplit("El Dr. Joan no vendrá.");
    testSplit("Distingguido Sr. Juan,");
    testSplit("Muy Hble. Sr. Presidente");
    testSplit("de San Nicolás (del s. XII; coro gótico del s. XIV) y de San Mateo.");
    testSplit("fue el 5o. clasificado.");
    testSplit("Fue el 5º. ", "Y el otro el 4º.");
    testSplit("Art. 2.1: Estarán obligados...");
    testSplit("Hasta las pp. 50-52.");
    testSplit("Hasta las pp. XI-XII.");
    testSplit("y es del vol. 3 de la colección");
    testSplit("En EE.UU.");
    testSplit("En EE. UU. por los DD. HH. después de los JJ. OO.");
    testSplit("En U.S.A. años 30.");
    testSplit("En U. S. A. años 30.");
    testSplit("P. ej. esto.");
    testSplit("Ahora p. ej. esto.");
    testSplit("Ahora p. e. esto.");
    testSplit("Son las 5hrs. del domingo.");
    testSplit("Son las 2as. jornadas.");
    testSplit("En EE.UU. esto no pasa.");
    testSplit("En EE. UU. esto no pasa.");
    testSplit("Me voy a EE. UU. ", "Buen viaje.");
    testSplit("Uno (ca. 2010), dos (c. 2011), tres (ca. XIX), cuatro (c. XX)");
    testSplit("Ayto. de Madrid.");

    // Exception to abbreviations    
    testSplit("Esto pasa el PP. ", "Pero, por otra parte,");
    testSplit("Cebolla, ajo, calabaza, etc. ", "Compramos en fruitería.");
    // Units
    testSplit("1 500 m/s. ", "Nacen en");
    testSplit("Son de 1 g. ", "Han sido acondicionadas.");
    testSplit("Son de 1 m. ", "Han sido acondicionadas.");
    testSplit("Vivían 50 h. ", "Después el pueblo creció.");
    testSplit("El acto será a las 15.30 h. de la tarde.");
    
    //Error: missing space. It is not split in order to trigger other errors. 
    testSplit("cuando G.Oueddei se convierte en líder");
    testSplit("el jesuita alemany J.E. Nithard");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
