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
package de.danielnaber.languagetool.tagging.ro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemmers.Lametyzator;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.BaseTagger;

/**
 * Romanian Part-of-speech tagger 
 * 
 * @author Ionuț Păduraru
 */
public class RomanianTagger extends BaseTagger {

	private String RESOURCE_FILENAME = "/resource/ro/romanian.dict";

	private Lametyzator morfologik;
	private static Locale roLocale = new Locale("ro");

	public final void setFileName() {
		System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICTIONARY,
				RESOURCE_FILENAME);
	}

	public RomanianTagger() {
		super();
		setFileName();
		setLocale(roLocale);
	}

	public RomanianTagger(String fileName) {
		super();
		RESOURCE_FILENAME = fileName;
		setLocale(roLocale);
	}

	@Override
	public final List<AnalyzedTokenReadings> tag(
			final List<String> sentenceTokens) throws IOException {
		String[] taggerTokens;

		final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
		int pos = 0;
		// caching Lametyzator instance - lazy init
		if (morfologik == null) {
			setFileName();
			morfologik = new Lametyzator();
		}

		for (final String word : sentenceTokens) {
			final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
			taggerTokens = morfologik.stemAndForm(word
					.toLowerCase());
			if (taggerTokens != null) {
				int i = 0;
				while (i < taggerTokens.length) {
					final String lemma = taggerTokens[i];
					final String[] tagsArr = taggerTokens[i + 1].split("\\+");

					for (final String currTag : tagsArr) {
						l.add(new AnalyzedToken(word, currTag, lemma, pos));
					}
					i = i + 2;
				}
			}

			if (taggerTokens == null) {
				l.add(new AnalyzedToken(word, null, pos));
			}
			pos += word.length();
			tokenReadings.add(new AnalyzedTokenReadings(l
					.toArray(new AnalyzedToken[l.size()])));
		}

		return tokenReadings;

	}

}
