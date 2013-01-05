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
package org.languagetool.tagging.ca;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.JLanguageTool;
import org.languagetool.tagging.BaseTagger;

/**
 * Catalan Tagger
 * 
 * Based on FreeLing tagger dictionary
 * 
 * @author Jaume Ortolà 
 */
public class CatalanTagger extends BaseTagger {

	private static final String DICT_FILENAME = "/ca/catalan.dict";
	private IStemmer morfologik;
	private final Locale plLocale = new Locale("ca");

	@Override
	public final String getFileName() {
		return DICT_FILENAME;
	}

	public CatalanTagger() {
		super();
		setLocale(new Locale("ca"));
	}

	public boolean existsWord(String word) throws IOException {
		// caching Lametyzator instance - lazy init
		if (morfologik == null) {
			final URL url = JLanguageTool.getDataBroker()
					.getFromResourceDirAsUrl(DICT_FILENAME);
			morfologik = new DictionaryLookup(Dictionary.read(url));
		}
		final String lowerWord = word.toLowerCase(plLocale);
		final List<WordData> posTagsFromDict = morfologik.lookup(lowerWord);
		if (posTagsFromDict.isEmpty())
			return false;
		else
			return true;
	}
}
