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
package org.languagetool.tokenizers.ro;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.Romanian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

/**
 * @author Ionuț Păduraru
 */
public class RomanianSentenceTokenizerTest {

  Language lang = new Romanian();
  // accept \n as paragraph:
  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(lang);
  // accept only \n\n as paragraph:
  private final SentenceTokenizer stokenizer2 = new SRXSentenceTokenizer(lang);

  @Before
  public final void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);
    stokenizer2.setSingleLineBreaksMarksParagraph(false);
  }

  @Test
  public final void testTokenize() {

    testSplit("Aceasta este o propozitie fara diacritice. ");
    testSplit("Aceasta este o fraza fara diacritice. ",
            "Propozitia a doua, tot fara diacritice. ");
    testSplit("Aceasta este o propoziție cu diacritice. ");
    testSplit("Aceasta este o propoziție cu diacritice. ",
            "Propoziția a doua, cu diacritice. ");

    testSplit("O propoziție! ", "Și încă o propoziție. ");
    testSplit("O propoziție... ", "Și încă o propoziție. ");
    testSplit("La adresa http://www.archeus.ro găsiți resurse lingvistice. ");
    testSplit("Data de 10.02.2009 nu trebuie să fie separator de propoziții. ");
    testSplit("Astăzi suntem în data de 07.05.2007. ");
    testSplit("Astăzi suntem în data de 07/05/2007. ");
    testSplit("La anumărul (1) avem puține informații. ");
    testSplit("To jest 1. wydanie.");
    testSplit("La anumărul 1. avem puține informații. ");
    testSplit("La anumărul 13. avem puține informații. ");
    testSplit("La anumărul 1.3.3 avem puține informații. ");

    testSplit("O singură propoziție... ");
    testSplit("Colegii mei s-au dus... ");
    testSplit("O singură propoziție!!! ");
    testSplit("O singură propoziție??? ");

    testSplit("Propoziții: una și alta. ");

    testSplit("Domnu' a plecat. ");
    testSplit("Profu' de istorie tre' să predea lecția. ");
    testSplit("Sal'tare! ");
    testSplit("'Neaţa! ");
    testSplit("Deodat'apare un urs. ");
    // accente
    testSplit("A făcut două cópii. ");
    testSplit("Ionel adúnă acum ceea ce Maria aduná înainte să vin eu. ");

    // incomplete sentences, need to work for on-thy-fly checking of texts:
    testSplit("Domnu' a plecat");
    testSplit("Domnu' a plecat. ",
            "El nu a plecat");

    testSplit("Se pot întâlni și abrevieri precum S.U.A. " +
            "sau B.C.R. într-o singură propoziție.");
    testSplit("Se pot întâlni și abrevieri precum S.U.A. sau B.C.R. ",
            "Aici sunt două propoziții.");
    testSplit("Același lucru aici... ", "Aici sunt două propoziții.");
    testSplit("Același lucru aici... dar cu o singură propoziție.");

    testSplit("„O propoziție!” ", "O alta.");
    testSplit("„O propoziție!!!” ", "O alta.");
    testSplit("„O propoziție?” ", "O alta.");
    testSplit("„O propoziție?!?” ", "O alta.");
    testSplit("«O propoziție!» ", "O alta.");
    testSplit("«O propoziție!!!» ", "O alta.");
    testSplit("«O propoziție?» ", "O alta.");
    testSplit("«O propoziție???» ", "O alta.");
    testSplit("«O propoziție?!?» ", "O alta.");
    testSplit("O primă propoziție. ", "(O alta.)");

    testSplit("A venit domnu' Vasile. ");
    testSplit("A venit domnu' acela. ");

    // one/two returns = paragraph = new sentence:
    TestTools.testSplit(new String[] { "A venit domnul\n\n", "Vasile." }, stokenizer2);
    TestTools.testSplit(new String[] { "A venit domnul\n", "Vasile." }, stokenizer);
    TestTools.testSplit(new String[] { "A venit domnu'\n\n", "Vasile." }, stokenizer2);
    TestTools.testSplit(new String[] { "A venit domnu'\n", "Vasile." }, stokenizer);
    // Missing space after sentence end:
    testSplit("El este din România!",
            "Acum e plecat cu afaceri.");

    testSplit("Temperatura este de 30°C.", "Este destul de cald.");
    testSplit("A alergat 50 m. ",
            "Deja a obosit.");

    // From the abbreviation list:
    testSplit("Pentru dvs. vom face o excepție.");
    testSplit("Pt. dumneavoastră vom face o excepție.");
    testSplit("Pt. dvs. vom face o excepție.");
    // din punct de vedere
    testSplit("A expus problema d.p.d.v. artistic.");
    testSplit("A expus problema dpdv. artistic.");
    // şi aşa mai departe.
    testSplit("Are mere, pere, șamd. dar nu are alune.");
    testSplit("Are mere, pere, ș.a.m.d. dar nu are alune.");
    testSplit("Are mere, pere, ș.a.m.d. ", "În schimb, nu are alune.");
    // şi celelalte
    testSplit("Are mere, pere, ş.c.l. dar nu are alune.");
    testSplit("Are mere, pere, ş.c.l. ", "Nu are alune.");
    // etc. et cetera
    testSplit("Are mere, pere, etc. dar nu are alune.");
    testSplit("Are mere, pere, etc. ", "Nu are alune.");
    // ş.a. - şi altele
    testSplit("Are mere, pere, ș.a. dar nu are alune.");

    // pag, leg, art
    testSplit("Lecția începe la pag. următoare și are trei pagini.");
    testSplit("Lecția începe la pag. 20 și are trei pagini.");
    testSplit("A acționat în conformitate cu lg. 144, art. 33.");
    testSplit("A acționat în conformitate cu leg. 144, art. 33.");
    testSplit("A acționat în conformitate cu legea nr. 11.");
    testSplit("Lupta a avut loc în anul 2000 î.H. și a durat trei ani.");

    // lunile anului, abreviate
    testSplit("Discuția a avut loc pe data de douăzeci aug. și a durat două ore.");
    testSplit("Discuția a avut loc pe data de douăzeci ian. și a durat două ore.");
    testSplit("Discuția a avut loc pe data de douăzeci feb. și a durat două ore.");
    testSplit("Discuția a avut loc pe data de douăzeci ian.", "A durat două ore.");

    // M.Ap.N. - Ministerul Apărării Nationale
    // there are 2 rules for this in segment.srx. Can this be done with only one rule?
    testSplit("A fost și la M.Ap.N. dar nu l-au primit. ");
    testSplit("A fost și la M.Ap.N. ", "Nu l-au primit. ");

    // sic!
    testSplit("Apo' da' tulai (sic!) că mult mai e de mers.");
    testSplit("Apo' da' tulai(sic!) că mult mai e de mers.");

    // […]
    testSplit("Aici este o frază […] mult prescurtată.");
    testSplit("Aici este o frază [...] mult prescurtată.");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer2);
  }

}
