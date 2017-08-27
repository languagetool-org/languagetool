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
package org.languagetool.language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.chunking.Chunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * Support for Serbian language
 */
public class Serbian extends Language implements AutoCloseable {

	private static final Language SERBIA_SERBIAN = new SerbiaSerbian();
	
	private Tagger tagger;
	private Chunker chunker;
	private SentenceTokenizer sentenceTokenizer;
	private Synthesizer synthesizer;
	private Disambiguator disambiguator;
	private WordTokenizer wordTokenizer;
	private LuceneLanguageModel languageModel;

	public Serbian() {
	}

	@Override
	public SentenceTokenizer getSentenceTokenizer() {
		if (sentenceTokenizer == null) {
			sentenceTokenizer = new SRXSentenceTokenizer(this);
		}
		return sentenceTokenizer;
	}

	@Override
	public String getName() {
		return "Serbian";
	}

	@Override
	public String getShortCode() {
		return "sr";
	}

	@Override
	public String[] getCountries() {
		return new String[] {};
	}

	@Override
	  public Language getDefaultLanguageVariant() {
	    return SERBIA_SERBIAN;
	  }
	
	@Override
	public Contributor[] getMaintainers() {
		return new Contributor[] { new Contributor("Золтан Чала (Csala Zoltán)") };
	}

	@Override
	public LanguageMaintainedState getMaintainedState() {
		return LanguageMaintainedState.ActivelyMaintained;
	}

	@Override
	public List<Rule> getRelevantRules(ResourceBundle messages)
			throws IOException {
		return Arrays.asList(
			new CommaWhitespaceRule(messages,
				Example.wrong("Није шија<marker> ,</marker> него врат."),
				Example.fixed("Није шија<marker>,</marker> него врат.")),
			new DoublePunctuationRule(messages),
			new GenericUnpairedBracketsRule(messages,
				Arrays.asList("[", "(", "{", "„", "„", "»", "«", "\""),
				Arrays.asList("]", ")", "}", "”", "“", "«", "»", "\"")),
			new UppercaseSentenceStartRule(messages, this,
				Example.wrong("Почела је школа. <marker>деца</marker> су поново села у клупе."),
                Example.fixed("Почела је школа. <marker>Деца</marker> су поново села у клупе.")),
			new WordRepeatRule(messages, this),
			new MultipleWhitespaceRule(messages, this)
			// TODO: Add Serbian-specific rules
		);
	}

	/**
	 * Closes the language model, if any.
	 * 
	 * @since 2.7
	 */
	@Override
	public void close() throws Exception {
		if (languageModel != null) {
			languageModel.close();
		}
	}

}
