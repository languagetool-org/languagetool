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
package de.danielnaber.languagetool.synthesis.ro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemmers.Lametyzator;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.SynthesizerTools;
import de.danielnaber.languagetool.tagging.ro.RomanianTagger;
import de.danielnaber.languagetool.tools.Tools;

/**
 * Romanian word form synthesizer. <br/>
 * 
 * Based on resources from www.archeus.ro
 * 
 * See note about the fsa dictionary at the beginning of
 * <code>RomanianTagger</code> file.
 * 
 * @author Ionuț Păduraru
 */

public class RomanianSynthesizer implements Synthesizer {

	private static final String RESOURCE_FILENAME = "/resource/ro/romanian_synth.dict";

	private static final String TAGS_FILE_NAME = "/resource/ro/romanian_tags.txt";

	private Lametyzator synthesizer;

	private ArrayList<String> possibleTags;

	private boolean fixDiacritics = true;

	private void setFileName() {
		System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICTIONARY,
				RESOURCE_FILENAME);
	}

	/**
	 * Get a form of a given AnalyzedToken, where the form is defined by a
	 * part-of-speech tag.
	 * 
	 * @param token
	 *            AnalyzedToken to be inflected.
	 * @param posTag
	 *            A desired part-of-speech tag.
	 * @return String value - inflected word.
	 */
	public String[] synthesize(final AnalyzedToken token, final String posTag)
			throws IOException {
		if (synthesizer == null) {
			setFileName();
			synthesizer = new Lametyzator();
		}
		String[] wordForms = null;
		wordForms = synthesizer.stem(_hideDiacritics(token.getLemma()) + "|"
				+ posTag);
		wordForms = _revealDiacritics(wordForms);
		return wordForms;
	}

	// TODO: avoid code duplication with DutchSynthesizer
	public String[] synthesize(final AnalyzedToken token, final String posTag,
			final boolean posTagRegExp) throws IOException {

		if (posTagRegExp) {
			if (possibleTags == null) {
				possibleTags = SynthesizerTools.loadWords(Tools
						.getStream(TAGS_FILE_NAME));
			}
			if (synthesizer == null) {
				setFileName();
				synthesizer = new Lametyzator();
			}
			final Pattern p = Pattern.compile(posTag);
			final ArrayList<String> results = new ArrayList<String>();
			for (final String tag : possibleTags) {
				final Matcher m = p.matcher(tag);
				if (m.matches()) {
					String[] wordForms = null;
					wordForms = synthesizer.stem(_hideDiacritics(token
							.getLemma())
							+ "|" + tag);
					if (wordForms != null) {
						results.addAll(Arrays.asList(wordForms));
					}
				}
			}
			String[] res = results.toArray(new String[results.size()]);
			return _revealDiacritics(res);
		}
		return synthesize(token, posTag);
	}

	public String getPosTagCorrection(final String posTag) {
		return posTag;
	}

	public String _revealDiacritics(String s) {
		if (isFixDiacritics())
			return RomanianTagger.revealDiacritics(s);
		return s;
	}

	/**
	 * Replace all digits with diacritics (the fsa dictionary was build this
	 * way). Depends on <code>isFixDiacritics</code> and uses the static method
	 * <code>RomanianTagger.revealDiacritics</code>.
	 * 
	 * @author Ionuț Păduraru
	 * @since 08.03.2009 21:50:19
	 * @param wordForms
	 * @return
	 */
	private String[] _revealDiacritics(String[] wordForms) {
		if ((null == wordForms) || (!isFixDiacritics()))
			return wordForms;
		String[] res = new String[wordForms.length];
		for (int i = 0; i < wordForms.length; i++) {
			res[i] = _revealDiacritics(wordForms[i]);
		}
		return res;
	}

	/**
	 * Replace all diacritics with digits (the fsa dictionary was build this
	 * way). Depends on <code>isFixDiacritics</code> and uses the static method
	 * <code>RomanianTagger.hideDiacritics</code>.
	 * 
	 * @author Ionuț Păduraru
	 * @since 08.03.2009 21:45:40
	 * @param s
	 * @return
	 */
	private String _hideDiacritics(String s) {
		if (isFixDiacritics())
			return RomanianTagger.hideDiacritics(s);
		return s;
	}

	/**
	 * A flag to indicate preprocessing of diacritics. See note about the fsa
	 * dictionary at the beginning of this file.
	 * 
	 * @author Ionuț Păduraru
	 * @since 08.03.2009 21:51:17
	 * @return
	 */
	public boolean isFixDiacritics() {
		return fixDiacritics;
	}

	public void setFixDiacritics(boolean fixDiacritics) {
		this.fixDiacritics = fixDiacritics;
	}
}
