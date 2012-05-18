/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.spelling.hunspell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

/**
 * A hunspell-based spellchecking-rule.
 * 
 * @author Marcin Miłkowski
 * 
 */
public class HunspellRule extends SpellingCheckRule {

	/**
	 * The dictionary file
	 */
	Hunspell.Dictionary dictionary = null;

	public HunspellRule(final ResourceBundle messages, final Language language)
			throws FileNotFoundException, UnsupportedEncodingException,
			UnsatisfiedLinkError, UnsupportedOperationException {
		super(messages, language);
		super.setCategory(new Category(messages.getString("category_typo")));

		// TODO: currently, the default dictionary is now
		// set to the first country variant on the list - so the order
		// in the Language class declaration is important!
		// we might support country variants in the near future

		final String shortDicPath = "/"
				+ language.getShortName()
				+ "/"
				+ language.getShortName()
				+ "_" 
				+ language.getCountryVariants()[0];
		
		// FIXME: need to change behavior of hunspell library, this is a hack to 
		// test hunspell
		
		String dictionaryPath = 
				JLanguageTool.getDataBroker().getFromResourceDirAsUrl(
				shortDicPath + ".dic").getPath();
		
		dictionaryPath = dictionaryPath.substring(0, dictionaryPath.length() - 4);

		// Note: the class will silently ignore the non-existence of
		// dictionaries!
		if (JLanguageTool.getDataBroker().getFromResourceDirAsUrl(
				shortDicPath + ".dic") != null) {
			dictionary = Hunspell.getInstance().getDictionary(dictionaryPath);
		}
	}

	@Override
	public String getId() {
		return "HUNSPELL_RULE";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RuleMatch[] match(AnalyzedSentence text) throws IOException {
		final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
		final AnalyzedTokenReadings[] tokens = text
				.getTokensWithoutWhitespace();

		// some languages might not have a dictionary, be silent about it
		if (dictionary == null)
			return null;

		// starting with the first token to skip the zero-length START_SENT
		for (int i = 1; i < tokens.length; i++) {
			final String word = tokens[i].getToken();
			boolean isAlphabetic = true;
			if (word.length() == 1) { // hunspell dicts usually do not contain punctuation
				isAlphabetic =
						Character.isAlphabetic(word.charAt(0));
			}
			if (isAlphabetic && dictionary.misspelled(word)) {
				final RuleMatch ruleMatch = new RuleMatch(this,
						tokens[i].getStartPos(), tokens[i].getStartPos() + word.length(),
						messages.getString("category_typo"),
						messages.getString("category_typo"));
				List<String> suggestions = dictionary.suggest(word);
				if (suggestions != null) {
					ruleMatch.setSuggestedReplacements(suggestions);
				}
				ruleMatches.add(ruleMatch);
			}
		}

		return toRuleMatchArray(ruleMatches);
	}

}
