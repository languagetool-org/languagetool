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

package org.languagetool.tokenizers.ca;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class CatalanSentenceTokenizerTest {

  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Catalan());

  @Test
  public final void testTokenize() {

    // Simple sentences
    testSplit("Això és una frase. ", "Això és una altra frase.");
    testSplit("Aquesta és l'egua. ", "Aquell és el cavall.");
    testSplit("Aquesta és l'egua? ", "Aquell és el cavall.");
    testSplit("Vols col·laborar? ", "Sí, i tant.");
    testSplit("Com vas d'il·lusió? ", "Bé, bé.");
    testSplit("Com vas d’il·lusió? ", "Bé, bé.");
    testSplit("És d’abans-d’ahir? ", "Bé, bé.");
    testSplit("És d’abans-d’ahir! ", "Bé, bé.");
    testSplit("Què vols dir? ", "Ja ho tinc!");
    testSplit("Què? ", "Ja ho tinc!");
    testSplit("Ah! ", "Ja ho tinc!");
    testSplit("Ja ho tinc! ", "Què vols dir?");
    testSplit("Us explicaré com va anar: ",
            "»La Maria va engegar el cotxe");
    testSplit("diu que va dir. ", "A mi em feia estrany.");
    testSplit("Són del s. III dC. ", "Són importants les pintures.");
    testSplit("Primera frase.[4] ", "Segona frase");
    
    // N., t.
    testSplit("Vés-te’n. ", "A mi em feia estrany.");  
    testSplit("Vés-te'n. ", "A mi em feia estrany.");
    testSplit("VÉS-TE'N. ", "A mi em feia estrany.");
    testSplit("Canten. ", "A mi em feia estrany.");
    testSplit("Desprèn. ", "A mi em feia estrany.");
    testSplit("(n. 3).");
    testSplit(" n. 3");
    testSplit("n. 3");
    testSplit("(\"n. 3\".");
    testSplit("En el t. 2 de la col·lecció");
    testSplit("Llança't. ", "Fes-ho.");
    
    // Initials
    testSplit("A l'atenció d'A. Comes.");
    testSplit("A l'atenció d'À. Comes.");
    testSplit("Núm. operació 220130000138.");

    // Ellipsis
    testSplit("el vi no és gens propi de monjos, amb tot...\" vetllant, això sí");
    testSplit("Desenganyeu-vos… ",
            "L’únic problema seriós de l'home en aquest món és el de subsistir.");
    testSplit("és clar… traduir és una feina endimoniada");
    testSplit("«El cordó del frare…» surt d'una manera desguitarrada");
    testSplit("convidar el seu heroi –del ram que sigui–… a prendre cafè.");

    // Abbreviations
    testSplit("No Mr. Spock sinó un altre.");
    testSplit("Vegeu el cap. 24 del llibre.");
    testSplit("Vegeu el cap. IX del llibre.");
    testSplit("Viu al núm. 24 del carrer de l'Hort.");
    testSplit("El Dr. Joan no vindrà.");
    testSplit("Distingit Sr. Joan,");
    testSplit("Molt Hble. Sr. President");
    testSplit("de Sant Nicolau (del s. XII; cor gòtic del s. XIV) i de Sant ");
    testSplit("Va ser el 5è. classificat.");
    testSplit("Va ser el 5è. ", "I l'altre el 4t.");
    testSplit("Art. 2.1: Són obligats els...");
    testSplit("Arriba fins a les pp. 50-52.");
    testSplit("Arriba fins a les pp. XI-XII.");
    testSplit("i no ho vol. ", "Malgrat que és així.");
    testSplit("i és del vol. 3 de la col·lecció");
    testSplit("Els EE. UU. són un país.");
    testSplit("Els EE.UU. són un país.");
    testSplit("Me'n vaig als EE.UU. ", "Bon viatge.");
    testSplit("Garcia, Joan (coords.)");
    testSplit("fins al curs de 8è. ", "\"No es pot oblidar allò\"");
    testSplit("fins al curs de 8è. ", "-No es pot oblidar allò");
    testSplit("Aprovació (ca. 2010), suspensió (c. 2011), segle (ca. XIX)");
    testSplit("La Dra. Ma. Victòria.");
    testSplit("la projectada Sta. Ma. de Gàllecs");

    // Exception to abbreviations
    testSplit("Ell és el número u. ", "Jo el dos.");
    testSplit("Té un trau al cap. ", "Cal portar-lo a l'hospital.");
    testSplit("Això passa en el PP. ", "Però, per altra banda,");
    testSplit("Ceba, all, carabassa, etc. ", "En comprem a la fruiteria.");
    // Units
    testSplit("1 500 m/s. ", "Neix a");
    testSplit("Són d'1 g. ", "Han estat condicionades.");
    testSplit("Són d'1 m. ", "Han estat condicionades.");
    testSplit("Hi vivien 50 h. ", "Després el poble va créixer.");
    testSplit("L'acte serà a les 15.30 h. de la vesprada.");
    testSplit("De 9:00 a 17:00 h. (aproximadament).");
    
    //Error: missing space. It is not split in order to trigger other errors. 
    testSplit("s'hi enfrontà quan G.Oueddei n'esdevingué líder");
    testSplit("el jesuïta alemany J.E. Nithard");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
