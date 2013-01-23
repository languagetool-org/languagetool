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
package org.languagetool.tagging.ro;

import junit.framework.TestCase;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.TestTools;
import org.languagetool.language.Romanian;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    TestTools.testDictionary(tagger, new Romanian());
  }
	
	/**
	 * 
	 * @author Ionuț Păduraru
	 * @since 08.03.2009 22:09:01
	 * @return
	 */
	protected RomanianTagger createTagger() {
		// override this if you need need another dictionary (a dictionary
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
		final List<AnalyzedTokenReadings> tags = tagger.tag(Arrays.asList(inflected));
		final StringBuilder allTags = new StringBuilder();
		boolean found = false;
		for (AnalyzedTokenReadings analyzedTokenReadings : tags) {
			final int length = analyzedTokenReadings.getReadingsLength();
			for (int i = 0; i < length; i++) {
				final AnalyzedToken token = analyzedTokenReadings.getAnalyzedToken(i);
				final String crtLemma = token.getLemma();
				final String crtPOSTag = token.getPOSTag();
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

	public RomanianTagger getTagger() {
		return tagger;
	}

	public WordTokenizer getTokenizer() {
		return tokenizer;
	}

}
