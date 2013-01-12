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

import junit.framework.TestCase;

import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class CatalanSentenceTokenizerTest extends TestCase {

	private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(
			Language.CATALAN);

	public final void testTokenize() {

		// Initials
		testSplit(new String[] { "A l'atenció d'A. Comes." });
		testSplit(new String[] { "A l'atenció d'À. Comes." });

		// Ellipsis
		testSplit(new String[] { "Desenganyeu-vos… ",
				"L’únic problema seriós de l'home en aquest món és el de subsistir." });
		testSplit(new String[] { "és clar… traduir és una feina endimoniada" });
		
		// Abbreviations
		testSplit(new String[] { "Viu al núm. 24 del carrer de l'Hort." });
		testSplit(new String[] { "El Dr. Joan no vindrà." });
		testSplit(new String[] { "Distingit Sr. Joan," });
		testSplit(new String[] { "Molt Hble. Sr. President" });

		// A problem solved
		testSplit(new String[] { "El pou d'Avall. ", "És bonic." }); 
		testSplit(new String[] { "El pou d’Avall. ", "És bonic." });
		testSplit(new String[] { "Ell viu a l'u. ", "Jo al dos." });
	}

	private void testSplit(final String[] sentences) {
		TestTools.testSplit(sentences, stokenizer);
	}

}
