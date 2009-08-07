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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

/**
 * <p>
 * Root class for RomanianTagger tests
 * </p>
 * <p>
 * Provides convenient methods to find specific lemma/pos
 * </p>
 * 
 * 
 * @author Ionuț Păduraru
 * @since 20.02.2009 19:36:32
 * 
 */
public abstract class RomanianTaggerTestAbs extends TestCase {

	private RomanianTagger tagger;
	private WordTokenizer tokenizer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp() {
		tagger = createTagger();
		tokenizer = new WordTokenizer();
	}

	public void testDictionary() throws IOException {
    final Dictionary dictionary = Dictionary.read(
        this.getClass().getResource(tagger.getFileName()));
    final DictionaryLookup dl = new DictionaryLookup(dictionary);
    for (WordData wd : dl) {
      if (wd.getTag() == null || wd.getTag().length() == 0) {
        System.err.println("**** Warning: the word " + wd.getWord() + "/" + wd.getStem() +" lacks a POS tag in the dictionary.");
      }
    }    
  }
	
	/**
	 * 
	 * @author Ionuț Păduraru
	 * @since 08.03.2009 22:09:01
	 * @return
	 */
	protected RomanianTagger createTagger() {
		// override this if you need need another dictionary (a disctionary
		// based on another file)
		return new RomanianTagger();
	}

	/**
	 * Verify if <code>inflected</code> contains the specified lemma and pos
	 * 
	 * @author Ionuț Păduraru
	 * @since 20.02.2009 19:17:54
	 * @param inflected
	 *            - input word, inflected form
	 * @param lemma
	 *            expected lemma
	 * @param posTag
	 *            expected tag for lemma
	 * @throws IOException
	 */
	protected void assertHasLemmaAndPos(String inflected, String lemma,
			String posTag) throws IOException {
		List<AnalyzedTokenReadings> tags = tagger.tag(createList(inflected));
		StringBuilder allTags = new StringBuilder();
		boolean found = false;
		for (AnalyzedTokenReadings analyzedTokenReadings : tags) {
			int length = analyzedTokenReadings.getReadingsLength();
			for (int i = 0; i < length; i++) {
				AnalyzedToken token = analyzedTokenReadings.getAnalyzedToken(i);
				String crtLemma = token.getLemma();
				String crtPOSTag = token.getPOSTag();
				allTags.append(String.format("[%s/%s]", crtLemma, crtPOSTag));
				found = ((null == lemma) || (lemma.equals(crtLemma)))
						&& ((null == posTag) || (posTag.equals(crtPOSTag)));
				if (found)
					break;
			} // for i
			if (found)
				break;
		} // foreach tag
		assertTrue(String.format("Lemma and POS not found for word [%s]! "
				+ "Expected [%s/%s]. Actual: %s", inflected, lemma, posTag,
				allTags.toString()), found);
	}

	/**
	 * Create a List containing some words
	 * 
	 * @author Ionuț Păduraru
	 * @since 20.02.2009 19:13:57
	 * @param words
	 * @return
	 */
	private List<String> createList(String... words) {
		List<String> res = new ArrayList<String>();
		for (String s : words) {
			res.add(s);
		}
		return res;
	}

	public RomanianTagger getTagger() {
		return tagger;
	}

	public WordTokenizer getTokenizer() {
		return tokenizer;
	}

}
