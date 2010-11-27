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
package de.danielnaber.languagetool.tokenizers.ro;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

/**
 * 
 * @author Ionuț Păduraru
 * @since 07.05.2009 10:28:59
 * 
 */
public class RomanianSentenceTokenizerTest extends TestCase {

	  // accept \n as paragraph:
	  private SentenceTokenizer stokenizer = new SRXSentenceTokenizer("ro");
	  // accept only \n\n as paragraph:
	  private SentenceTokenizer stokenizer2 = new SRXSentenceTokenizer("ro");

	  public final void setUp() {
	    stokenizer.setSingleLineBreaksMarksParagraph(true);
	    stokenizer2.setSingleLineBreaksMarksParagraph(false);
	  }

	  public final void testTokenize() {
	    
	    testSplit(new String[] { "Aceasta este o propozitie fara diacritice. " });
	    testSplit(new String[] { "Aceasta este o fraza fara diacritice. ", 
	    		"Propozitia a doua, tot fara diacritice. " });
	    testSplit(new String[] { "Aceasta este o propoziție cu diacritice. " });
	    testSplit(new String[] { "Aceasta este o propoziție cu diacritice. ", 
	    		"Propoziția a doua, cu diacritice. " });

	    testSplit(new String[] { "O propoziție! ", "Și încă o propoziție. "});
	    testSplit(new String[] { "O propoziție... ", "Și încă o propoziție. "});
	    testSplit(new String[] { "La adresa http://www.archeus.ro găsiți resurse lingvistice. "});
	    testSplit(new String[] { "Data de 10.02.2009 nu trebuie să fie separator de propoziții. "});
	    testSplit(new String[] { "Astăzi suntem în data de 07.05.2007. "});
	    testSplit(new String[] { "Astăzi suntem în data de 07/05/2007. "});
	    testSplit(new String[] { "La anumărul (1) avem puține informații. "});
	    testSplit(new String[] { "To jest 1. wydanie." });
	    testSplit(new String[] { "La anumărul 1. avem puține informații. "});
	    testSplit(new String[] { "La anumărul 13. avem puține informații. "});
	    testSplit(new String[] { "La anumărul 1.3.3 avem puține informații. "});
	    
	    testSplit(new String[] { "O singură propoziție... "});
	    testSplit(new String[] { "Colegii mei s-au dus... "});	    
	    testSplit(new String[] { "O singură propoziție!!! "});
	    testSplit(new String[] { "O singură propoziție??? "});
	    
	    testSplit(new String[] { "Propoziții: una și alta. "});
	    
	    testSplit(new String[] { "Domnu' a plecat. "});
	    testSplit(new String[] { "Profu' de istorie tre' să predea lecția. "});
	    testSplit(new String[] { "Sal'tare! "});
	    testSplit(new String[] { "'Neaţa! "});
	    testSplit(new String[] { "Deodat'apare un urs. "});
	    // accente
	    testSplit(new String[] { "A făcut două cópii. "});
	    testSplit(new String[] { "Ionel adúnă acum ceea ce Maria aduná înainte să vin eu. "});
	    
	    // incomplete sentences, need to work for on-thy-fly checking of texts:
	    testSplit(new String[] { "Domnu' a plecat" });
	    testSplit(new String[] { "Domnu' a plecat. ",
	        "El nu a plecat" });

	    testSplit(new String[] { "Se pot întâlni și abrevieri precum S.U.A. " +
	    		"sau B.C.R. într-o singură propoziție." });
	    testSplit(new String[] { "Se pot întâlni și abrevieri precum S.U.A. sau B.C.R. ",
	    		"Aici sunt două propoziții." });
	    testSplit(new String[] { "Același lucru aici... ", "Aici sunt două propoziții." });
	    testSplit(new String[] { "Același lucru aici... dar cu o singură propoziție." });
	    
	    testSplit(new String[] { "„O propoziție!” ", "O alta." });
	    testSplit(new String[] { "„O propoziție!!!” ", "O alta." });
	    testSplit(new String[] { "„O propoziție?” ", "O alta." });
	    testSplit(new String[] { "„O propoziție?!?” ", "O alta." });
	    testSplit(new String[] { "«O propoziție!» ", "O alta." });
	    testSplit(new String[] { "«O propoziție!!!» ", "O alta." });
	    testSplit(new String[] { "«O propoziție?» ", "O alta." });
	    testSplit(new String[] { "«O propoziție???» ", "O alta." });
	    testSplit(new String[] { "«O propoziție?!?» ", "O alta." });
	    testSplit(new String[] { "O primă propoziție. ", "(O alta.)" });

	    testSplit(new String[] { "A venit domnu' Vasile. " });
	    testSplit(new String[] { "A venit domnu' acela. " });
	    
	    // one/two returns = paragraph = new sentence:
	    TestTools.testSplit(new String[] { "A venit domnul\n\n", "Vasile." }, stokenizer2);
	    TestTools.testSplit(new String[] { "A venit domnul\n", "Vasile." }, stokenizer);
	    TestTools.testSplit(new String[] { "A venit domnu'\n\n", "Vasile." }, stokenizer2);
	    TestTools.testSplit(new String[] { "A venit domnu'\n", "Vasile." }, stokenizer);
	    // Missing space after sentence end:
	    testSplit(new String[] { "El este din România!",
	        "Acum e plecat cu afaceri." });

	    testSplit(new String[] { "Temperatura este de 30°C.", "Este destul de cald." });
	    testSplit(new String[] { "A alergat 50 m. ",
	        "Deja a obosit." });

	    // From the abbreviation list:
	    testSplit(new String[] { "Pentru dvs. vom face o excepție." });
	    testSplit(new String[] { "Pt. dumneavoastră vom face o excepție." });
	    testSplit(new String[] { "Pt. dvs. vom face o excepție." });
	    // din punct de vedere
	    testSplit(new String[] { "A expus problema d.p.d.v. artistic." });
	    testSplit(new String[] { "A expus problema dpdv. artistic." });
	    // şi aşa mai departe.
	    testSplit(new String[] { "Are mere, pere, șamd. dar nu are alune." });
	    testSplit(new String[] { "Are mere, pere, ș.a.m.d. dar nu are alune." });
	    testSplit(new String[] { "Are mere, pere, ș.a.m.d. ", "În schimb, nu are alune." });
	    // şi celelalte
	    testSplit(new String[] { "Are mere, pere, ş.c.l. dar nu are alune." });
	    testSplit(new String[] { "Are mere, pere, ş.c.l. ", "Nu are alune." });
	    // etc. et cetera
	    testSplit(new String[] { "Are mere, pere, etc. dar nu are alune." });
	    testSplit(new String[] { "Are mere, pere, etc. ", "Nu are alune." });
	    // ş.a. - şi altele
	    testSplit(new String[] { "Are mere, pere, ș.a. dar nu are alune." });
	    // M.Ap.N. - Ministerul Apărării Nationale
	    // there are 2 rules for this in segment.srx. Can this be done with only one rule?
	    testSplit(new String[] { "A fost și la M.Ap.N. dar nu l-au primit. " });
	    testSplit(new String[] { "A fost și la M.Ap.N. ", "Nu l-au primit. " });

	    // sic!
	    testSplit(new String[] { "Apo' da' tulai (sic!) că mult mai e de mers." });
	    testSplit(new String[] { "Apo' da' tulai(sic!) că mult mai e de mers." });
	    
	    // […]
	    testSplit(new String[] { "Aici este o frază […] mult prescurtată." });
	    testSplit(new String[] { "Aici este o frază [...] mult prescurtată." });
	  }

	  private final void testSplit(final String[] sentences) {
	    TestTools.testSplit(sentences, stokenizer2);
	  }
  
}
