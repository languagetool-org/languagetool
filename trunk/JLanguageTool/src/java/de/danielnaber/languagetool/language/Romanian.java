/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.language;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.ro.RomanianSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.ro.RomanianRuleDisambiguator;
import de.danielnaber.languagetool.tagging.ro.RomanianTagger;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.ro.RomanianWordTokenizer;

/**
 * 
 * @author Ionuț Păduraru
 * @since 24.02.2009 22:18:21
 */
public class Romanian extends Language {

	private static final String[] COUNTRIES = { "RO" };

	private Tagger tagger = new RomanianTagger();
	private Synthesizer synthesizer = new RomanianSynthesizer();
	private Disambiguator disambiguator = new RomanianRuleDisambiguator();
	private Tokenizer wdTokenizer = new RomanianWordTokenizer();

	public Locale getLocale() {
		return new Locale(getShortName());
	}

	public String getName() {
		return "Română";
	}

	public String getShortName() {
		return "ro";
	}

	@Override
	public String[] getCountryVariants() {
		return COUNTRIES;
	}

	public Tagger getTagger() {
		return tagger;
	}

	public Contributor[] getMaintainers() {
		Contributor contributor = new Contributor("Ionuț Păduraru");
		contributor.setUrl("http://www.archeus.ro");
		return new Contributor[] { contributor };
	}

	public Set<String> getRelevantRuleIDs() {
		Set<String> ids = new HashSet<String>();
		ids.add("COMMA_PARENTHESIS_WHITESPACE");
		ids.add("DOUBLE_PUNCTUATION");
		ids.add("UPPERCASE_SENTENCE_START");
		ids.add("WHITESPACE_RULE");
		ids.add("UNPAIRED_BRACKETS");
		ids.add("UPPERCASE_SENTENCE_START");
		ids.add("WORD_REPEAT_RULE");
		// specific to romanian: none so far

		return ids;
	}

	public final Synthesizer getSynthesizer() {
		return synthesizer;
	}

	public final Disambiguator getDisambiguator() {
		return disambiguator;
	}

	public final Tokenizer getWordTokenizer() {
		return wdTokenizer;
	}
}
