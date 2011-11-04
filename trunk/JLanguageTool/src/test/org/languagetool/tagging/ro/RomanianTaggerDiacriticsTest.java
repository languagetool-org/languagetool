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
package de.danielnaber.languagetool.tagging.ro;

/**
 * 
 * These tests are kept to make sure UTF-8 dictionaries are correctly read.<br/>
 * Prior to morfologik 1.1.4 some words containing diacritics were not correctly
 * returned.
 * 
 * @author Ionuț Păduraru
 * @since 08.03.2009 19:25:50
 */
public class RomanianTaggerDiacriticsTest extends RomanianTaggerTestAbs {

	/**
	 * "test_diacritics.dict" was built from a simple input file :
	 * <p>
	 * cușcă cușcă 001
	 * </p>
	 * <p>
	 * cartea carte 000
	 * </p>
	 * <p>
	 * mergeam merge 001
	 * </p>
	 * <p>
	 * merseserăm merge 002
	 * </p>
	 * <p>
	 * cuțit cuțit 001
	 * </p>
	 * <p>
	 * cuțitul cuțit 002
	 * </p>
	 * 
	 * @author Ionuț Păduraru
	 * @since 08.03.2009 19:15:59
	 * @throws Exception
	 */
	@Override
	protected RomanianTagger createTagger() {
		RomanianTagger res = new RomanianTagger(
				"/ro/test_diacritics.dict");
		return res;
	}

	/**
	 * Prior to morfologik 1.1.4: For "merseserăm" the lemma is incorect: "mege"
	 * instead of "merge". If the dictionary is used from
	 * command-line(/fsa_morph -d ...), the correct lemma is returned.
	 * 
	 * @author Ionuț Păduraru
	 * @since 08.03.2009 19:25:59
	 * @throws Exception
	 */
	public void testTaggerMerseseram() throws Exception {
		// these tests are using "test_diacritics.dict"
		assertHasLemmaAndPos("făcusem", "face", "004");
		assertHasLemmaAndPos("cuțitul", "cuțit", "002");
		// make sure lemma is correct (POS is hard-coded, not important)
		assertHasLemmaAndPos("merseserăm", "merge", "002");
	}

	/**
	 * 
	 * @author Ionuț Păduraru
	 * @since 24.03.2009 21:39:25
	 * @throws Exception
	 */
	public void testTaggerCuscaCutit() throws Exception {
		// these tests are using "test_diacritics.dict"
		// all these are correct, they are here just to prove that "some" words
		// are corectly returned

		assertHasLemmaAndPos("cușcă", "cușcă", "001");
		assertHasLemmaAndPos("cuțit", "cuțit", "001");
		assertHasLemmaAndPos("cuțitul", "cuțit", "002");
	}

}
