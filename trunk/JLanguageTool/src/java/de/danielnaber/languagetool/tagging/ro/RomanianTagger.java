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
 * Romanian Part-of-speech tagger <br/>
 * <b>Note</b> about fsa dictionary: current fsa dictionary was <i>bilt with no
 * diacritics</i>: all diacritics are replaced with digits. From the user point
 * of view (the user who writex xml rule) this is not visible, the tagger will
 * return the corect form (with diacritics). All this process is necesary to
 * avoid a posible bug on fsa dictionary access (or some place esle): a correct
 * dictionary (with diacritics) can only be used correctly from command line
 * ('fsa_morph' from fsa package), when used from java some words are not
 * corectly returned. <br/>
 * This workaround will be removed once the fsa problem is resolved. <br/>
 * Same workaround is used on <code>RomanianSynthesizer</code>.
 * 
 * @author Ionuț Păduraru
 */
public class RomanianTagger extends BaseTagger {
	private String RESOURCE_FILENAME = "/resource/ro/romanian.dict";
	private static RomanianTagger instance = null;

	private Lametyzator morfologik;
	private static Locale roLocale = new Locale("ro");

	private boolean fixDiacritics = true;

	public void setFileName() {
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
			// word in the dictionary contain no diacritics: all
			// diacritics are replaced with 1,2,etc
			taggerTokens = morfologik.stemAndForm(_hideDiacritics(word
					.toLowerCase()));
			if (taggerTokens != null) {
				int i = 0;
				while (i < taggerTokens.length) {
					// word in the dictionary contain no diacritics: all
					// diacritics are replaced with 1,2,etc
					final String lemma = _revealDiacritics(taggerTokens[i]);
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

	public static boolean isWord(String token) {
		for (int i = 0; i < token.length(); i++) {
			char c = token.charAt(i);
			if (Character.isLetter(c) || Character.isDigit(c))
				return true;
		}
		return false;
	}

	public static RomanianTagger getInstance() {
		if (null == instance) {
			instance = new RomanianTagger();
		}
		return instance;
	}

	/**
	 * Replace all digits with diacritics (the fsa dictionary was build this
	 * way)
	 * 
	 * @author Ionuț Păduraru
	 * @since 24.02.2009 22:14:38
	 * @param s
	 * @return
	 */
	public static String revealDiacritics(String s) {
		if (null == s)
			return s;
		char[] res = new char[s.length()];
		for (int i = 0; i < res.length; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '1':
				c = 'ș';
				break;
			case '2':
				c = 'ț';
				break;
			case '3':
				c = 'ă';
				break;
			case '4':
				c = 'â';
				break;
			case '5':
				c = 'î';
				break;

			default:
				break;
			}
			res[i] = c;
		}
		return new String(res);
	}

	/**
	 * Replace all diacritics with digits (the fsa dictionary was build this
	 * way).
	 * 
	 * @author Ionuț Păduraru
	 * @since 24.02.2009 22:16:07
	 * @param s
	 * @return
	 */
	public static String hideDiacritics(String s) {
		if (null == s)
			return s;
		char[] res = new char[s.length()];
		for (int i = 0; i < res.length; i++) {
			char c = s.charAt(i);
			switch (c) {
			case 'ș':
				c = '1';
				break;
			case 'ț':
				c = '2';
				break;
			case 'ă':
				c = '3';
				break;
			case 'â':
				c = '4';
				break;
			case 'î':
				c = '5';
				break;

			default:
				break;
			}
			res[i] = c;
		}
		return new String(res);
	}

	/**
	 * Replace all digits with diacritics (the fsa dictionary was build this
	 * way). Depends on <code>isFixDiacritics</code> and uses the static method
	 * <code>revealDiacritics</code>.
	 * 
	 * @author Ionuț Păduraru
	 * @since 14.03.2009 21:34:43
	 * @param s
	 * @return
	 */
	public String _revealDiacritics(String s) {
		if (isFixDiacritics())
			return revealDiacritics(s);
		return s;
	}

	/**
	 * Replace all diacritics with digits (the fsa dictionary was build this
	 * way). Depends on <code>isFixDiacritics</code> and uses the static method
	 * <code>hideDiacritics</code>.
	 * 
	 * @author Ionuț Păduraru
	 * @since 14.03.2009 21:35:58
	 * @param s
	 * @return
	 */
	private String _hideDiacritics(String s) {
		if (isFixDiacritics())
			return hideDiacritics(s);
		return s;
	}

	/**
	 * A flag to indicate preprocessing of diacritics. See note about the fsa
	 * dictionary at the beginning of this file.
	 * 
	 * @author Ionuț Păduraru
	 * @since 14.03.2009 21:36:49
	 * @return
	 */
	public boolean isFixDiacritics() {
		return fixDiacritics;
	}

	public void setFixDiacritics(boolean fixDiacritics) {
		this.fixDiacritics = fixDiacritics;
	}
}
